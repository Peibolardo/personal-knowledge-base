package backend.controller.interfaces;

import backend.dto.ChatRequestDTO;
import backend.dto.ChatResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface IOpenAICommandController {

    /**
     * POST /api/v1/chat
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
    ResponseEntity<ChatResponseDTO> sendMessageToApi(@RequestBody @Valid ChatRequestDTO chatRequestDTO);

}
