package backend.service.command;

import backend.dto.ChatRequestDTO;
import backend.dto.ChatResponseDTO;
import backend.dto.ChatContextDTO;
import backend.dto.MessageDTO;
import backend.exception.customExceptions.ConflictException;
import backend.exception.customExceptions.ExternalServiceOperationException;
import backend.exception.customExceptions.InvalidRequestException;
import backend.exception.customExceptions.UnreachableExternalServiceException;
import backend.mapper.MessageChatRequestMapper;
import backend.mapper.MessageChatResponseMapper;
import backend.mapper.MessageMapper;
import backend.model.Conversation;
import backend.model.Message;
import backend.repository.ConversationRepository;
import backend.repository.MessageRepository;
import backend.service.interfaces.OpenAICommandService;
import backend.sse.DTOs.ChatStreamContentDTO;
import backend.sse.DTOs.ChatStreamDoneDTO;
import backend.sse.DTOs.ChatStreamErrorDTO;
import backend.sse.DTOs.ChatStreamMetadataDTO;
import backend.sse.utils.ChatStreamEventDeserializer;
import backend.sse.utils.ChatStreamEventSerializer;
import backend.utils.externalServiceServices.ExternalAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static backend.utils.IdValidator.validateUuids;

@Service
public class OpenAICommandServiceImpl implements OpenAICommandService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAICommandServiceImpl.class);
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ExternalAiService externalAiService;
    private final MessageChatRequestMapper chatRequestMapper;
    private final MessageChatResponseMapper chatResponseMapper;
    private final MessageMapper messageMapper;
    private final ChatStreamEventSerializer chatStreamEventSerializer;
    private final ChatStreamEventDeserializer chatStreamEventDeserializer;

    private final int maxTokens;
    private final int summaryThreshold;
    private static final int CONTEXT_BATCH_SIZE = 9;
    private final TaskExecutor summaryTaskExecutor;

    public OpenAICommandServiceImpl(MessageRepository messageRepository,
                                    ConversationRepository conversationRepository,
                                    ExternalAiService externalAiService,
                                    MessageChatRequestMapper chatRequestMapper,
                                    MessageChatResponseMapper chatResponseMapper,
                                    MessageMapper messageMapper,
                                    ChatStreamEventSerializer chatStreamEventSerializer,
                                    ChatStreamEventDeserializer chatStreamEventDeserializer,
                                    @Value("${context.max-tokens-request}") int maxTokens,
                                    @Value("${summary.threshold}") int summaryThreshold,
                                    @Qualifier("summaryTaskExecutor") TaskExecutor summaryTaskExecutor) {

        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.externalAiService = externalAiService;
        this.chatRequestMapper = chatRequestMapper;
        this.chatResponseMapper = chatResponseMapper;
        this.messageMapper = messageMapper;
        this.chatStreamEventSerializer = chatStreamEventSerializer;
        this.chatStreamEventDeserializer = chatStreamEventDeserializer;
        this.maxTokens = maxTokens;
        this.summaryThreshold = summaryThreshold;
        this.summaryTaskExecutor = summaryTaskExecutor;

    }

    /**
     * Function to send the context window of 10 messages max to the AI-API order from oldest to newest
     * @param chatRequestDTO The new message sent by the user
     * @return ChatResponseDTO with the AI response.
     *
     * @throws InvalidRequestException if input IDs or DTO data are invalid.
     * @throws UnreachableExternalServiceException If the API Service is not online
     * @throws ExternalServiceOperationException If an exception occurs in the API Service
     */
    public ChatResponseDTO sendMessageToApi(ChatRequestDTO chatRequestDTO){

        // 1. Prepare the context messages
        PreparedMessages preparedMessages = prepareMessages(chatRequestDTO);

        // 2. Save the requestMessage Message object
        Message messageRequest = preparedMessages.requestMessage;

        // 2.Send the context to the API and try to receive the response
        ChatResponseDTO responseDTO = externalAiService.sendMessageToAi(preparedMessages.context);

        // 4.Check if the response exists
        if(responseDTO == null){
            throw new ConflictException("There was no response for this request");
        }

        // 5. Save the request message into the database
        messageRequest.setTokenCount(responseDTO.getTokensInputMessage());
        messageRepository.save(messageRequest);

        // 6. Save the response message
        Message messageResponse = chatResponseMapper.toMessage(responseDTO);
        messageResponse.setConversation(messageRequest.getConversation());
        messageResponse.setRole("assistant");
        messageRepository.save(messageResponse);

        // 7. Set conversationId before sending back to the front
        responseDTO.setConversationId(messageRequest.getConversation().getId());

        // 8. Return the response DTO
        return responseDTO;

    }

    /**
     * Sends a chat request to the AI service and returns its response as an SSE stream.
     *
     * Execution flow:
     * 1. Prepare the conversation context and the user message before making the AI request.
     * 2. Use Flux.defer() to delay the AI request and stream initialization until subscription.
     *    A new StreamState is created for each subscription.
     * 3. Use concatMap() to process incoming SSE events sequentially through handleEvent().
     * 4. Use onErrorResume() to distinguish between errors before the first emitted event
     *    and errors occurring after streaming has started. Before the first event, propagate
     *    the error; afterward, emit an SSE error event.
     * 5. Use doOnNext() to track whether an event has been emitted and whether a terminal
     *    event (done/error) has been received.
     * 6. Use takeUntil() to forward the terminal event and then stop the upstream stream.
     * 7. Use concatWith() to check whether the stream ended without a terminal event.
     *    If so, emit a fallback SSE error event; otherwise, complete without emitting anything.
     *
     * Important:
     * - prepareMessages() executes before the deferred publisher is subscribed to.
     * - started tracks event emission, not whether the HTTP response has been committed.
     * - concatMap() preserves sequential event processing, which is necessary to accumulate
     *   the assistant's response before persisting it when the done event arrives.
     *
     * @param chatRequestDTO the user's chat request
     * @return a Flux of Server-Sent Events to be forwarded to the client
     */
    public Flux<ServerSentEvent<String>> sendMessageToApiStreaming(ChatRequestDTO chatRequestDTO){

        // 1. Prepare the context messages
        PreparedMessages preparedMessages = prepareMessages(chatRequestDTO);

        // 2. Set the summary and context messages into the Context DTO to send
        Conversation conversation = preparedMessages.requestMessage.getConversation();
        ChatContextDTO chatContextDTO = new ChatContextDTO(conversation.getSummary(), preparedMessages.context);

        // 2. Save the requestMessage Message object
        Message messageRequest = preparedMessages.requestMessage;

        // 3. Send the context to the API and receive the Stream/Flux response
        return Flux.defer(() -> {

            StreamState state = new StreamState();   // one per subscription

            return externalAiService.streamingMessageToAi(chatContextDTO)
                    .concatMap(sse -> handleEvent(sse, messageRequest, state))
                    .onErrorResume(e -> {
                        // nothing sent yet: let GlobalExceptionHandler answer with an HTTP status
                        if (!state.started) {
                            return Flux.error(e);
                        }
                        logger.error("AI stream failed mid-stream", e);
                        return Flux.just(createErrorEvent("STREAM_INTERRUPTED", "The AI stream failed."));
                    })
                    .doOnNext(e -> {
                        state.started = true;

                        if (isTerminal(e)) state.terminalSeen = true;
                    })
                    .takeUntil(this::isTerminal)
                    .concatWith(Flux.defer(() -> state.terminalSeen
                            ? Flux.empty()
                            : Flux.just(createErrorEvent("STREAM_INTERRUPTED",
                            "The AI stream ended unexpectedly."))));
        });

    }

    /**
     * Function to prepare the messages to send to the AI
     * @param chatRequestDTO DTO of the request send by the user
     * @return PreparedMessages record with the Message of requestMessage object and the List of Message for the context
     */
    private PreparedMessages prepareMessages(ChatRequestDTO chatRequestDTO){

        // 1. Initialize a list to store the context window
        List<MessageDTO> messageDTOList = new ArrayList<MessageDTO>();

        // 2. Map the requestDTO message to a Message object and setConversation due to ignored in mapping
        Message messageRequest = chatRequestMapper.toEntity(chatRequestDTO);
        messageRequest.setConversation(getOrCreateConversation(chatRequestDTO.getConversationId(), chatRequestDTO.getInput()));
        // 3. Set user as the role of the sender
        messageRequest.setRole("user");

        // 4. Map the Message Object to the DTO to send
        MessageDTO messageDTO = messageMapper.toDTO(messageRequest);

        // 5.1 If conversationId null send just the new message
        if(isNewConversation(chatRequestDTO)){
            messageDTOList.add(messageDTO);
        }
        // 5.2 Else add the context messages then the new one
        else{
            // 5.2.1 Checks if the conversationId is valid
            validateUuids(chatRequestDTO.getConversationId());

            // 5.2.2 Create variables to hold the token counting, the page to retrieve the pageable data and checker if the context threshold was reached
            boolean budgetReached = false;
            int tokenCount = 0;
            int actualPage = 0;
            List<Message> completeContext = new ArrayList<Message>();

            // 5.2.3 Retrieve the context messages and add it until no messages can be retrieved or the sum is higher than the Max permitted
            while(!budgetReached){

                List<Message> contextMessages = messageRepository.getContextMessages(messageRequest.getConversation().getId(), PageRequest.of(actualPage, CONTEXT_BATCH_SIZE) );

                if(contextMessages.isEmpty()){
                    break;
                }

                for(Message message: contextMessages ) {

                    if(tokenCount + message.getTokenCount() <= this.maxTokens){
                        completeContext.add(message);
                        tokenCount += message.getTokenCount();
                    }
                    else{
                        budgetReached = true;
                        break;
                    }

                }

                if (!budgetReached) {
                    actualPage++;
                }

            }

            // 5.2.2 order properly the messages reversing the order fetched
            Collections.reverse(completeContext);
            List<MessageDTO> contextMessagesDTO = messageMapper.toDtoList(completeContext);
            messageDTOList.addAll(contextMessagesDTO);
            messageDTOList.add(messageDTO);
        }

        return new PreparedMessages(messageRequest, messageDTOList);

    }

    /**
     * Groups the user message entity and the prepared context messages
     * so both can be returned from prepareMessages().
     */
    private record PreparedMessages(Message requestMessage, List<MessageDTO> context){}

    /**
     * Method to check if the message belongs to the first of a conversation or not
     * @param chatRequestDTO message from the frontend
     * @return If the messages is the first or not
     */
    private boolean isNewConversation(ChatRequestDTO chatRequestDTO){
        return chatRequestDTO.getConversationId() == null;
    }

    /**
     * Method to get or create a new conversation for the messages
     * It checks if there is any conversationId passed down and create one if it doesn't, otherwise it fetches it from the database
     * @param conversationId The unique identifier of the conversation
     * @param message The first message from the conversation
     * @return Conversation Object
     */
    private Conversation getOrCreateConversation(String conversationId, String message) {
        if (conversationId == null) {
            Conversation newConversation = Conversation.builder().title(generateTitle(message)).build();
            return conversationRepository.save(newConversation);
        }
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
    }

    /**
     * Method to Generate a title automatically when created a Conversation
     * @param message The first message from the conversation
     * @return The title created
     */
    private String generateTitle(String message) {
        return message.length() > 30 ? message.substring(0, 30) + "..." : message;
    }


    /**
     * Processes an SSE event received from the AI service.
     *
     * Execution flow:
     * 1. Validate that the event contains data.
     * 2. Read the event name and deserialize its JSON payload into the corresponding DTO.
     * 3. Handle the event according to its type:
     *    - Metadata: update the request message's input token count and forward metadata.
     *    - Content: append the generated text to the accumulated assistant response and forward it.
     *    - Error: forward the AI service's error event.
     *    - Done: persist the user and assistant messages, then forward the completion event.
     *    - Unsupported event: emit an SSE error event.
     * 4. Handle deserialization, event-processing and persistence errors through the
     *    appropriate exception or reactive error-handling mechanisms.
     *
     * Reactive concepts:
     * - Mono represents zero or one emitted item; Flux represents zero or many items.
     * - Mono.fromCallable() defers the persistence operation until subscription and emits the completion event only if persistence succeeds.
     * - subscribeOn(Schedulers.boundedElastic()) schedules blocking JPA operations on a scheduler intended for blocking work.
     * - flux() converts the Mono into a Flux to match this method's return type.
     * - onErrorResume() handles a reactive persistence failure and emits an SSE error event.
     *
     * @param sse the SSE event received from the AI service
     * @param messageRequest the user's message to persist with the assistant's response
     * @param state the state shared across events in the current stream subscription
     * @return a Flux containing the SSE event or error event to forward to the client
     */
    private Flux<ServerSentEvent<String>> handleEvent(ServerSentEvent<String> sse, Message messageRequest, StreamState state){

        // 1. Check if there is any data in the event
        if (sse.data() == null) {
            return Flux.empty();
        }

        // 2. Get the event type field from the Event
        String eventType = sse.event() == null ? "" : sse.event();

        // 3. Create an object and deserialize it into an EventDTO
        Object deserializedObject;

        try{
            deserializedObject = chatStreamEventDeserializer.deserialize(eventType, sse.data());
        }
        catch (IllegalArgumentException e){
            return Flux.just(createErrorEvent("UPSTREAM_NOT_ACCEPTABLE_OBJECT", "The AI service returned an unsupported event."));
        }
        catch (IllegalStateException e){
            return Flux.just(createErrorEvent("UPSTREAM_NOT_ACCEPTABLE_JSON", "The AI service returned an malformed JSON event."));
        }

        try{
            // 4. Handle the object depending on its event
            switch(deserializedObject){
                // 4.1 Save tokens of the request into the messageRequest and add conversationId to the metadata to serialize and send to the frontend
                case ChatStreamMetadataDTO metadata:
                    messageRequest.setTokenCount(metadata.getTokensInputMessage());
                    return Flux.just(createMetadataEvent(messageRequest.getConversation().getId(), metadata.getTokensInputMessage(), metadata.getModel()));

                // 4.2 Saves the chunk content into the actual responseMessage String
                case ChatStreamContentDTO content:
                    state.responseMessage.append(content.getContent());
                    return Flux.just(sse);

                case ChatStreamErrorDTO errorDTO:

                    return Flux.just(sse);

                // 4.4 Return using mono as a reactive publisher
                case ChatStreamDoneDTO doneDTO:

                    return Mono.fromCallable(() -> {
                                // Blocked persistence by Schedulers.boundedElastic()
                                persistMessages(messageRequest, doneDTO, state);
                                // Helper method to set summarizeConversation() into a TaskExecutor thread manager
                                triggerSummarization(messageRequest.getConversation().getId());
                                // Return the SSE event
                                return sse;                                   // forward the original done event
                            })
                            .subscribeOn(Schedulers.boundedElastic())          // blocking JPA lives here
                            .flux()
                            .onErrorResume(e -> {
                                logger.error("Could not save the conversation turn", e);
                                return Flux.just(createErrorEvent("PERSISTENCE_ERROR",
                                        "The response could not be saved."));
                            });

                default: {
                    return Flux.just(createErrorEvent(
                            "UNSUPPORTED_EVENT_OBJECT",
                            "The deserialized object has an unsupported type."
                    ));
                }

            }

        }catch(Exception e){
            logger.error("Could not handle the sse event");
            return Flux.just(createErrorEvent("EVENT_HANDLING_ERROR", "Could not process an AI stream event."));
        }

    }

    /**
     * Helper function to build a ServerSentEvent for ChatStreamErrorDTO
     * @param code exception code
     * @param message exception message
     * @return ServerSentEvent With the Error Event Object serialized
     */
    private ServerSentEvent<String> createErrorEvent(String code, String message){

        ChatStreamErrorDTO chatStreamErrorDTO = new ChatStreamErrorDTO(code, message);

        return ServerSentEvent.<String>builder()
                .event("error")
                .data(chatStreamEventSerializer.serialize(chatStreamErrorDTO))
                .build();

    }

    /**
     * Helper function to build a ServerSentEvent for ChatStreamMetadataDTO
     * @param conversationId conversationId of the message
     * @param tokensInputMessage tokens used in the request message
     * @param model of the LLM used
     * @return ServerSentEvent With the Metadata Event Object serialized
     */
    private ServerSentEvent<String> createMetadataEvent(String conversationId, Integer tokensInputMessage, String model){

        ChatStreamMetadataDTO chatStreamMetadataDTO = new ChatStreamMetadataDTO(conversationId, tokensInputMessage, model);

        return ServerSentEvent.<String>builder()
                .event("metadata")
                .data(chatStreamEventSerializer.serialize(chatStreamMetadataDTO))
                .build();

    }

    /**
     * Helper to save into the database the request and response Message object
     * @param requestMessage The Request Message object
     * @param doneDTO Stream Done event with the information tokens for the response
     * @param streamState The response content
     */
    private void persistMessages(Message requestMessage, ChatStreamDoneDTO doneDTO, StreamState streamState){

        // 1. Save the request message after set the tokens in the database
        messageRepository.save(requestMessage);

        // 2. Build the Response Message object and save it in the database
        Message responseMessage = new Message();
        responseMessage.setContent(streamState.responseMessage.toString());
        responseMessage.setConversation(requestMessage.getConversation());
        responseMessage.setTokenCount(doneDTO.getTokensCompletion());
        responseMessage.setRole("assistant");
        messageRepository.save(responseMessage);

    }

    /**
     * Generates or updates the conversation summary when the accumulated token count of unsummarized messages reaches the configured summary threshold.
     *
     * Retrieves messages in batches, starting with the most recent, until it reaches the last summarized message or there are no more messages.
     * The selected messages are then reordered chronologically and sent to the AI service together with the existing summary, if any.
     *
     * After the AI service successfully generates the updated summary, the conversation's summary and last summarized message ID are updated and
     * persisted. If the threshold is not reached, no changes are made.
     *
     * @param conversation the conversation whose summary should be generated or updated
     */
    private void summarizeConversation(Conversation conversation){

        if(conversation != null) {

            // Create variables to hold the token counting, the page to retrieve the pageable data and checker if the context threshold was reached
            boolean reachedLimit = false;
            int tokenCount = 0;
            int actualPage = 0;

            // New message list to add to the summary
            List<Message> completeContext = new ArrayList<Message>();

            //Contains the last message included in the summary
            Message newestMessageIncluded  = null;

            while(!reachedLimit) {

                List<Message> newMessages = messageRepository.getContextMessages(conversation.getId(), PageRequest.of(actualPage, CONTEXT_BATCH_SIZE));

                // Check if there is no more messages to iterate
                if(newMessages.isEmpty()){
                    reachedLimit = true;
                    break;
                }

                for(Message message: newMessages){

                    if( conversation.getLastSummarizedMessageId() != null && message.getId().equals(conversation.getLastSummarizedMessageId())){
                        reachedLimit = true;
                        break;
                    }

                    completeContext.add(message);
                    tokenCount += message.getTokenCount();

                    // Sets the message as the first included
                    if (newestMessageIncluded == null) {
                        newestMessageIncluded = message;
                    }
                }

                if(!reachedLimit ){
                    actualPage ++;
                }

            }
            // If the tokens counted is above the threshold then call the AI Service to summarize the conversation with the new messages and old summary
            if(tokenCount >= summaryThreshold && newestMessageIncluded != null){

                // Order the messages from oldest to newest
                Collections.reverse(completeContext);

                List <MessageDTO> completeContextDTO = messageMapper.toDtoList(completeContext);
                // Build the DTO to send with summary and the list of new messages
                ChatContextDTO conversationSummary = new ChatContextDTO(conversation.getSummary(), completeContextDTO);

                logger.info("Summarizing {} new messages. Oldest included ID: {}, previous checkpoint: {}", completeContext.size(), newestMessageIncluded.getId(), conversation.getLastSummarizedMessageId());

                // Receive the new summary to save
                String newSummary = externalAiService.sendSummaryToTheLLM(conversationSummary);

                conversation.setSummary(newSummary);
                conversation.setLastSummarizedMessageId(newestMessageIncluded.getId());

                conversationRepository.save(conversation);

            }

        }

    }

    /**
     * Helper to schedules conversation summarization as a background task.
     * Reloads the conversation inside the task to avoid sharing a potentiallystale JPA entity between threads.
     *
     * Summarization failures are logged without interrupting the completed chat.
     *
     * @param conversationId ID of the conversation to summarize
     */
    private void triggerSummarization(String conversationId) {
        try {
            summaryTaskExecutor.execute(() -> {
                try {
                    Conversation conversation = conversationRepository.findById(conversationId).orElse(null);

                    if (conversation == null) {
                        logger.warn("Conversation {} not found for summarization", conversationId);
                        return;
                    }

                    summarizeConversation(conversation);

                } catch (Exception e) {
                    logger.error("Conversation summarization failed for conversation {}", conversationId, e);
                }
            });
        } catch (TaskRejectedException e) {
            logger.error("Could not schedule summarization for conversation {}", conversationId, e);
        }
    }

    /**
     * Small helper to check if the event flux is and error or done one
     * @param e Event received
     * @return Return if the event is a terminal one
     */
    private boolean isTerminal(ServerSentEvent<String> e) {
        return "done".equals(e.event()) || "error".equals(e.event());
    }

    /**
     * Small class to create a String to save the message for every Stream request.
     * Created every request.
     */
    private static class StreamState {

        private final StringBuilder responseMessage = new StringBuilder();
        private boolean started;
        private boolean terminalSeen;

    }


}
