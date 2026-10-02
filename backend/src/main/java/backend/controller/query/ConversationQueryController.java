package backend.controller.query;

import backend.dto.ConversationDTO;
import backend.dto.MessageDTO;
import backend.service.interfaces.ConversationQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("${api.endpoint.backend}/conversations")
public class ConversationQueryController {

    private static final Logger logger = LoggerFactory.getLogger(ConversationQueryController.class);
    private final ConversationQueryService conversationQueryService;

    public ConversationQueryController(ConversationQueryService conversationQueryService) {
        this.conversationQueryService = conversationQueryService;
    }

    /**
     * GET /api/v1/conversations
     * Purpose: Retrieves all the conversations from the database.
     * Responses:
     * 200 OK: A JSON array of Layer objects.
     * 404 Not Found: If there are no conversations.
     * 500 Internal Server Error.
     *
     * @return A ResponseEntity containing a list of LayerDTONew objects.
     */
    @GetMapping
    public ResponseEntity<List<ConversationDTO>> getAllConversations(){

        logger.info("Received request to retrieve all Conversation objects from the database");
        List<ConversationDTO> conversationDTOList = conversationQueryService.getAllConversations();
        logger.info("Succesfully retrieved all conversation from the database");
        return ResponseEntity.ok(conversationDTOList);

    }

    /**
     * GET /api/v1/conversations/{conversationId}/messages
     * Purpose: Retrieves all the messages belonging to a specific conversation.
     * Path Parameters:
     * conversationId(string, required): The unique identifier of the conversation.
     * Responses:
     * 200 OK: A JSON array of Layer objects.
     * 400 Bad Request: If the conversationId is wrong.
     * 404 Not Found: If the conversation does not exist or there are no messages for the conversation.
     * 500 Internal Server Error.
     *
     * @return A ResponseEntity containing a list of LayerDTONew objects.
     */
    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<List<MessageDTO>> getMessagesByConversationId(@PathVariable String conversationId) {
        logger.info("Received request to retrieve messages for conversation ID: {}", conversationId);
        List<MessageDTO> messageDTOList = conversationQueryService.getMessagesByConversationId(conversationId);
        logger.info("Successfully retrieved {} messages for conversation ID: {}", messageDTOList.size(), conversationId);
        return ResponseEntity.ok(messageDTOList);
    }

}
