package backend.controller.command;

import backend.dto.ChatRequestDTO;
import backend.dto.ChatResponseDTO;
import backend.service.interfaces.OpenAICommandService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("$backend.service.url.api}")
public class OpenAICommandController {

    private static final Logger logger = LoggerFactory.getLogger(OpenAICommandController.class);

    private OpenAICommandService openAICommandService;

    /**
     * POST /chat
     * Purpose: Send a message to the API
     * Request Body:
     * chatRequestDTO : (Required) ChatRequestDTO
     * Responses:
     * 200 OK: If successful, returns the chat response from the API.
     * 400 Bad Request: if missing parameters or invalid data.
     * 500 Internal Server Error.
     *
     * @param chatRequestDTO The ChatRequestDTO with the information to send to the API.
     * @return A ResponseEntity containing the ChatResponseDTO with the response information.
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponseDTO> sendMessageToApi(@RequestBody @Valid ChatRequestDTO chatRequestDTO){

        logger.info("Received request to send a message to the api");
        ChatResponseDTO chatResponseDTO = openAICommandService.sendMessageToApi(chatRequestDTO);
        logger.info("Successfully sent message to the API");
        return ResponseEntity.ok(chatResponseDTO);

    }


}
