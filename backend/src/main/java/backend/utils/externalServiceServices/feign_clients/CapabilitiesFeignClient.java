package backend.utils.externalServiceServices.feign_clients;

import backend.dto.capabilities.ModelCapabilities;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "capabilities-api-service", url = "${python.service.url.api}")
public interface CapabilitiesFeignClient {

    String PREFIX = "/capabilities";

    /**
     * Calls the Python AI service to retrieve the configured model limits and prompt token counts.
     *
     * @return the model and prompt capabilities reported by the AI service
     */
    @GetMapping(PREFIX)
    ModelCapabilities getCapabilities();

}
