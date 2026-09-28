package backend.utils.externalServiceServices;

import backend.dto.ChatResponseDTO;
import backend.dto.MessageDTO;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "ai-api-service", url = "${python.service.url.api}")
public interface AiApiFeignClient {

    String PREFIX = "/chat";

    /**
     * Calls the python service and retrieves the response
     */
    @PostMapping(PREFIX)
    ChatResponseDTO sendMessageToAi(@RequestBody @Valid List<MessageDTO> messageDTOList);
}

