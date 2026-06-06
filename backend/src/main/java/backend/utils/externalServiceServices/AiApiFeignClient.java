package backend.utils.externalServiceServices;

import backend.dto.ChatRequestDTO;
import backend.dto.ChatResponseDTO;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ai-api-service", url = "${python.service.url.api}")
public interface AiApiFeignClient {

    String PREFIX = "/chat";

    /**
     * Calls the python service and retrieves the response
     */
    @PostMapping(PREFIX)
    ChatResponseDTO getActiveMissions(@RequestBody @Valid ChatRequestDTO chatRequestDTO);
}

