package backend.service.interfaces;

import backend.dto.ConversationDTO;
import backend.dto.MessageDTO;
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



    List<MessageDTO> getMessagesByConversationId(String conversationId);

}
