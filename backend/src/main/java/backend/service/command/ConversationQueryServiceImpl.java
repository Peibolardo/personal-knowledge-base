package backend.service.command;

import backend.dto.ConversationDTO;
import backend.dto.MessageDTO;
import backend.exception.customExceptions.InvalidRequestException;
import backend.exception.customExceptions.ResourceNotFoundException;
import backend.mapper.ConversationMapper;
import backend.mapper.MessageMapper;
import backend.model.Conversation;
import backend.model.Message;
import backend.repository.ConversationRepository;
import backend.repository.MessageRepository;
import backend.service.interfaces.ConversationQueryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static backend.utils.IdValidator.validateUuids;


@Service
@RequiredArgsConstructor
public class ConversationQueryServiceImpl implements ConversationQueryService {

    private static final Logger logger = LoggerFactory.getLogger(ConversationQueryServiceImpl.class);
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;

    /**
     * Method to get all the Conversation objects from the database
     * @return DTO List with all conversations
     *
     * @throws ResourceNotFoundException If there are no conversations in the database
     */
    public List<ConversationDTO> getAllConversations(){

        // 1. Retrieve all the conversation from the database
        List<Conversation> conversationList = conversationRepository.findAll();

        // 2. Check if the List is empty
        if(conversationList.isEmpty()){
            throw new ResourceNotFoundException("There is no conversation in the database");
        }

        // 3. Return the DTO list with all conversations
        return conversationMapper.toDTOList(conversationList);

    }

    /**
     * Method to retrieve all the messages belonging to a conversation.
     * @param conversationId The unique identifier of the conversation.
     * @return DTO List with all the Messages that belong to a conversation.
     * @throws InvalidRequestException If the conversationId is not UUID4.
     * @throws ResourceNotFoundException If there are no messages for that conversation in the database
     */
    public List<MessageDTO> getMessagesByConversationId(String conversationId){

        // 1.Check if the conversationId is correct
        validateUuids(conversationId);

        // 2. Retrieve all the conversation from the database
        List<Message> messageList = messageRepository.getConversationMessages(conversationId);

        // 3. Check if the List is empty
        if(messageList.isEmpty()){
            throw new ResourceNotFoundException("There are no messages for that conversation in the database");
        }

        // 4. Return the DTO list with all conversations
        return messageMapper.toDtoList(messageList);

    }

}
