package backend.service.interfaces;

import backend.dto.ConversationDTO;
import backend.dto.MessageDTO;
import backend.exception.customExceptions.InvalidRequestException;
import backend.exception.customExceptions.ResourceNotFoundException;

import java.util.List;

public interface ConversationQueryService {

    /**
     * Method to get all the Conversation objects from the database
     * @return DTO List with all conversations
     *
     * @throws ResourceNotFoundException If there are no conversations in the database
     */
    List<ConversationDTO> getAllConversations();


    /**
     * Method to retrieve all the messages belonging to a conversation.
     * @param conversationId The unique identifier of the conversation.
     * @return DTO List with all the Messages that belong to a conversation.
     * @throws InvalidRequestException If the conversationId is not UUID4.
     * @throws ResourceNotFoundException If there are no messages for that conversation in the database
     */
    List<MessageDTO> getMessagesByConversationId(String conversationId);

}
