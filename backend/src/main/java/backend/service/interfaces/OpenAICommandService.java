package backend.service.interfaces;

import backend.dto.ChatRequestDTO;
import backend.dto.ChatResponseDTO;

public interface OpenAICommandService {

    ChatResponseDTO sendMessageToApi(ChatRequestDTO chatRequestDTO);

}
