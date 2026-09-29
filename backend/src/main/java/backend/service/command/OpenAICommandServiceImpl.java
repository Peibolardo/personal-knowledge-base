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
import backend.utils.externalServiceServices.ExternalAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

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

    public OpenAICommandServiceImpl(MessageRepository messageRepository, ConversationRepository conversationRepository, ExternalAiService externalAiService, MessageChatRequestMapper chatRequestMapper, MessageChatResponseMapper chatResponseMapper, MessageMapper messageMapper) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.externalAiService = externalAiService;
        this.chatRequestMapper = chatRequestMapper;
        this.chatResponseMapper = chatResponseMapper;
        this.messageMapper = messageMapper;
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

        // 1. Initialize a list to store the context window
        List<MessageDTO> messageDTOList = new ArrayList<MessageDTO>();

        // 2. Map the requestDTO message to a Message object and setConversation due to ignored in mapping
        Message messageRequest = chatRequestMapper.toEntity(chatRequestDTO);
        messageRequest.setConversation(getOrCreateConversation(chatRequestDTO.getConversationId()));
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

        // 6.Send the context to the API and try to receive the response
        ChatResponseDTO responseDTO = externalAiService.sendMessageToAi(messageDTOList);

        // 7.Check if the response exists
        if(responseDTO == null){
            throw new ConflictException("There was no response for this request");
        }

        // 8. Save the request message into the database
        messageRepository.save(messageRequest);

        // 9. Save the response message
        Message messageResponse = chatResponseMapper.toMessage(responseDTO);
        messageResponse.setConversation(messageRequest.getConversation());
        messageResponse.setRole("assistant");
        messageRepository.save(messageResponse);

        // 10. Set conversationId before sending back to the front
        responseDTO.setConversationId(messageRequest.getConversation().getId());

        return responseDTO;

    }

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
     * It checks if there is any conversationId passed down and create one if it doesnt, otherwise it fetches it from the database
     * @param conversationId The unique identifier of the conversation
     * @return Conversation Object
     */
    private Conversation getOrCreateConversation(String conversationId) {
        if (conversationId == null) {
            Conversation newConversation = Conversation.builder().build();
            return conversationRepository.save(newConversation);
        }
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
    }

}
