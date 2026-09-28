package backend.service.command;

import backend.dto.ChatRequestDTO;
import backend.dto.ChatResponseDTO;
import backend.dto.MessageDTO;
import backend.exception.customExceptions.ConflictException;
import backend.exception.customExceptions.ExternalServiceOperationException;
import backend.exception.customExceptions.InvalidRequestException;
import backend.exception.customExceptions.UnreachableExternalServiceException;
import backend.mapper.MessageChatRequestMapper;
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
import java.util.List;

import static backend.utils.IdValidator.validateUuids;

@Service
public class OpenAICommandServiceImpl implements OpenAICommandService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAICommandServiceImpl.class);
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ExternalAiService externalAiService;
    private final MessageChatRequestMapper chatRequestMapper;
    private final MessageMapper messageMapper;

    public OpenAICommandServiceImpl(MessageRepository messageRepository, ConversationRepository conversationRepository, ExternalAiService externalAiService, MessageChatRequestMapper chatRequestMapper, MessageMapper messageMapper) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.externalAiService = externalAiService;
        this.chatRequestMapper = chatRequestMapper;
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

        // 2. Map the requestDTO message to a Message object and setConversation dut to ignored in mapping
        Message message = chatRequestMapper.toEntity(chatRequestDTO);
        message.setConversation(conversationRepository.findById(chatRequestDTO.getConversationId()).orElse(null));

        // 3. Set user as the role of the sender
        message.setRole("user");

        // 4. Map the Message Object to the DTO to send
        MessageDTO messageDTO = messageMapper.toDTO(message);

        // 5.1 If conversationId null send just the new message
        if(!checkConversationExists(chatRequestDTO)){
            messageDTOList.add(messageDTO);
        }
        // 5.2 Else add the context messages then the new one
        else{
            // 5.2.1 Checks if the conversationId is valid
            validateUuids(chatRequestDTO.getConversationId());
            List<Message> contextMessages = messageRepository.getContextMessages(message.getConversation().getId(), PageRequest.of(0, 9));
            List<MessageDTO> contextMessagesDTO = messageMapper.toDtoList(contextMessages);
            messageDTOList.addAll(contextMessagesDTO);
            messageDTOList.add(messageDTO);
        }

        // 6.Send the context to the API and try to receive the response
        ChatResponseDTO responseDTO = externalAiService.sendMessageToAi(messageDTOList);

        // 2.Check if the response exists
        if(responseDTO == null){
            throw new ConflictException("There was no response for this request");
        }

        return responseDTO;

    }

    /**
     * Method to check if the message belongs to the first of a conversation or not
     * @param chatRequestDTO message from the frontend
     * @return If the messages is the first or not
     */
    private boolean checkConversationExists(ChatRequestDTO chatRequestDTO){
        return chatRequestDTO.getConversationId() == null;
    }

}
