package backend.service.command;

import backend.dto.ChatRequestDTO;
import backend.dto.ChatResponseDTO;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

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

    public OpenAICommandServiceImpl(MessageRepository messageRepository, ConversationRepository conversationRepository, ExternalAiService externalAiService, MessageChatRequestMapper chatRequestMapper, MessageChatResponseMapper chatResponseMapper, MessageMapper messageMapper, ChatStreamEventSerializer chatStreamEventSerializer, ChatStreamEventDeserializer chatStreamEventDeserializer) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.externalAiService = externalAiService;
        this.chatRequestMapper = chatRequestMapper;
        this.chatResponseMapper = chatResponseMapper;
        this.messageMapper = messageMapper;
        this.chatStreamEventSerializer = chatStreamEventSerializer;
        this.chatStreamEventDeserializer = chatStreamEventDeserializer;
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

        // 1. Prepare the context
        PreparedContext preparedContext = prepareContext(chatRequestDTO);

        // 2. Save the requestMessage Message object
        Message messageRequest = preparedContext.requestMessage;

        // 2.Send the context to the API and try to receive the response
        ChatResponseDTO responseDTO = externalAiService.sendMessageToAi(preparedContext.context);

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


    public Flux<ServerSentEvent<String>> sendMessageToApiStreaming(ChatRequestDTO chatRequestDTO){

        // 1. Prepare the context
        PreparedContext preparedContext = prepareContext(chatRequestDTO);

        // 2. Save the requestMessage Message object
        Message messageRequest = preparedContext.requestMessage;

        StreamState state = new StreamState();

        // 3. Send the context to the API and receive the Stream/Flux response
        return externalAiService.streamingMessageToAi(preparedContext.context)
                .concatMap(sse -> handleEvent(sse, messageRequest, state));

    }




    /**
     * Function to prepare the context window to send to the AI
     * @param chatRequestDTO DTO of the request send by the user
     * @return PreparedContext record with the Message of requestMessage object and the List of Message with the context
     */
    private PreparedContext prepareContext(ChatRequestDTO chatRequestDTO){

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
            List<Message> contextMessages = messageRepository.getContextMessages(messageRequest.getConversation().getId(), PageRequest.of(0, 9));
            // 5.2.2 order properly the messages reversing the order fetched
            Collections.reverse(contextMessages);
            List<MessageDTO> contextMessagesDTO = messageMapper.toDtoList(contextMessages);
            messageDTOList.addAll(contextMessagesDTO);
            messageDTOList.add(messageDTO);
        }

        return new PreparedContext(messageRequest, messageDTOList);

    }

    /**
     * Groups the user message entity and the prepared context
     * so both can be returned from prepareContext().
     */
    private record PreparedContext(Message requestMessage, List<MessageDTO> context){}

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


    private Flux<ServerSentEvent<String>> handleEvent(ServerSentEvent<String> sse, Message messageRequest, StreamState state){

        // Check if there is any data in the event
        if (sse.data() == null) {
            return Flux.empty();
        }
        String eventType = sse.event() == null ? "" : sse.event();

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

            switch(deserializedObject){
                // Save tokens of the request into the messageRequest and add conversationId to the metadata to serialize and send to the frontend
                case ChatStreamMetadataDTO metadata:
                    messageRequest.setTokenCount(metadata.getTokensInputMessage());
                    messageRepository.save(messageRequest);
                    return Flux.just(createMetadataEvent(messageRequest.getConversation().getId(), metadata.getTokensInputMessage(), metadata.getModel()));

                // Saves the chunk content into the actual responseMessage String
                case ChatStreamContentDTO content:
                    state.responseMessage.append(content.getContent());
                    return Flux.just(sse);

                case ChatStreamErrorDTO errorDTO:

                    return Flux.just(sse);

                case ChatStreamDoneDTO doneDTO:

                    Message requestMessage = new Message();
                    requestMessage.setContent(state.responseMessage.toString());
                    requestMessage.setConversation(messageRequest.getConversation());
                    requestMessage.setTokenCount(doneDTO.getTokensCompletion());
                    requestMessage.setRole("assistant");
                    messageRepository.save(requestMessage);
                    return Flux.just(sse);

                default: {
                    return Flux.just(createErrorEvent(
                            "UNSUPPORTED_EVENT_OBJECT",
                            "The deserialized object has an unsupported type."
                    ));
                }

            }

        }catch(Exception e){
            logger.error("Could not handle the sse event");
            throw new InvalidRequestException("Could not handle the SSE event");
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
     * @param model of thje LLM used
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
     * Small class to create a String to save the message for every Stream request
     */
    private static class StreamState {

        private final StringBuilder responseMessage = new StringBuilder();

    }


}
