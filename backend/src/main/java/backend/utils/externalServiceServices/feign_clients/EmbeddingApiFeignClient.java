package backend.utils.externalServiceServices.feign_clients;

import backend.dto.EmbeddingRequestDTO;
import backend.dto.EmbeddingResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "embedding-api-service", url = "${python.service.url.api}")
public interface EmbeddingApiFeignClient {

    String PREFIX = "/embeddings";

    /**
     * Calls the python service embedding endpoints to generate the embedding chunks for a message
     */
    @PostMapping(PREFIX + "/generate")
    EmbeddingResponseDTO embedMessageByChunks(EmbeddingRequestDTO embeddingRequestDTO);

}
