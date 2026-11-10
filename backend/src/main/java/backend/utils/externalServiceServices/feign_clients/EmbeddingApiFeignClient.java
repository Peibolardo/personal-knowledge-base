package backend.utils.externalServiceServices.feign_clients;

import backend.dto.EmbeddingRequestDTO;
import backend.dto.EmbeddingResponseDTO;
import backend.dto.QueryEmbeddingResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "embedding-api-service", url = "${python.service.url.api}")
public interface EmbeddingApiFeignClient {

    String PREFIX = "/embeddings";

    /**
     * Calls the python service embedding endpoints to generate the embedding chunks for a message
     */
    @PostMapping(PREFIX + "/generate")
    EmbeddingResponseDTO embedMessageByChunks(EmbeddingRequestDTO embeddingRequestDTO);

    /**
     * Sends the complete user query to the Python embedding endpoint without chunking it.
     *
     * @param query the full query text to embed
     * @return the query embedding and the model used to generate it
     */
    @PostMapping(PREFIX + "/query")
    QueryEmbeddingResponseDTO embedQuery(@RequestBody String query);

}
