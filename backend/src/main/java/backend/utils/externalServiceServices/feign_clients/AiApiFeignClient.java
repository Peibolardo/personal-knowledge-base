package backend.utils.externalServiceServices.feign_clients;

import backend.dto.ChatResponseDTO;
import backend.dto.ChatContextDTO;
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

    /**
     * Calls the python service with the context and receive the new summarization
     */
    @PostMapping(PREFIX + "/summarize")
    String sendSummaryToTheLLM(@RequestBody @Valid ChatContextDTO chatContextDTO);

}

