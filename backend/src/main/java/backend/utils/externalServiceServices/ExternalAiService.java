package backend.utils.externalServiceServices;

import backend.dto.capabilities.ChatModelCapabilities;
import backend.dto.capabilities.EmbeddingCapabilities;
import backend.dto.*;
import backend.dto.chat.ChatContextDTO;
import backend.dto.chat.ChatResponseDTO;
import backend.dto.embeddings.EmbeddingRequestDTO;
import backend.dto.embeddings.EmbeddingResponseDTO;
import backend.dto.embeddings.QueryEmbeddingResponseDTO;
import backend.exception.customExceptions.ExternalServiceOperationException;
import backend.exception.customExceptions.UnreachableExternalServiceException;
import backend.utils.externalServiceServices.feign_clients.AiApiFeignClient;
import backend.utils.externalServiceServices.feign_clients.CapabilitiesFeignClient;
import backend.utils.externalServiceServices.feign_clients.EmbeddingApiFeignClient;
import feign.RetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Flux;


import java.util.List;

@Service
public class ExternalAiService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalAiService.class);
    private final AiApiFeignClient aiApiFeignClient;
    private final CapabilitiesFeignClient capabilitiesFeignClient;
    private final EmbeddingApiFeignClient embeddingApiFeignClient;
    private final WebClient aiWebClient;

    public ExternalAiService(
            AiApiFeignClient aiApiFeignClient,
            CapabilitiesFeignClient capabilitiesFeignClient,
            EmbeddingApiFeignClient embeddingApiFeignClient,
            WebClient aiWebClient
    ) {
        this.aiApiFeignClient = aiApiFeignClient;
        this.capabilitiesFeignClient = capabilitiesFeignClient;
        this.embeddingApiFeignClient = embeddingApiFeignClient;
        this.aiWebClient = aiWebClient;
    }

    /**
     * Attempting to send the message to the Api for the AI
     * @param messageDTOList The DTO list with the context information
     * @return The response from the AI
     */
    public ChatResponseDTO sendMessageToAi(List<MessageDTO> messageDTOList){

        logger.info("Attempting to send a message to the Python service");

        try{
            return aiApiFeignClient.sendMessageToAi(messageDTOList);
        }
        catch(RetryableException e){
            logger.warn("Failed to reach the API AI Service when trying to access OpenAI API: {}", e.toString());
            throw new UnreachableExternalServiceException("External service is unreachable..");
        }
         catch (Exception e) {
            logger.warn("Unexpected error response from the API AI Service when trying to access OpenAI API with returning exception: {}", e.toString());
            throw new ExternalServiceOperationException("Unexpected external service error");
        }
    }

    /**
     * Sends the conversation context to the Python AI service and returns its response as an SSE stream.
     *
     * <p>The request is sent as JSON to the {@code /chat/stream} endpoint, and the response is
     * received as a stream of Server-Sent Events (SSE).</p>
     *
     * <p>Processing steps:</p>
     * <ul>
     *     <li>{@code uri}: specifies the Python AI service endpoint.</li>
     *
     *     <li>{@code contentType}: indicates that the request body contains JSON.</li>
     *
     *     <li>{@code accept}: indicates that the client expects an SSE stream.</li>
     *
     *     <li>{@code bodyValue}: serializes the conversation messages into the request body.</li>
     *
     *     <li>{@code retrieve}: executes the request and prepares the response for processing.</li>
     *
     *     <li>{@code onStatus}: handles HTTP 4xx and 5xx responses by creating an
     *         {@code ExternalServiceOperationException} containing the status code and error body.</li>
     *
     *     <li>{@code bodyToFlux}: converts the response body into a reactive stream of
     *         {@code ServerSentEvent<String>} objects. Each event contains its event name and
     *         data as a string; JSON deserialization is handled elsewhere.</li>
     *
     *     <li>{@code onErrorMap}: translates {@code WebClientRequestException} errors into
     *         {@code UnreachableExternalServiceException} when communication with the AI service fails.</li>
     * </ul>
     *
     * @param chatContextDTO the context DTO with the conversation messages and summary to sent to the AI service
     * @return a Flux emitting SSE events as they arrive from the AI service
     */
    public Flux<ServerSentEvent<String>> streamingMessageToAi(ChatContextDTO chatContextDTO){

            return aiWebClient.post()
                    .uri("/chat/stream")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .bodyValue(chatContextDTO)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp ->
                            resp.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .map(body -> {
                                        int status = resp.statusCode().value();

                                        String message = "AI service error " + status + ": " + body;

                                        if (status == 502 || status == 503 || status == 504) {
                                            return new UnreachableExternalServiceException(message);
                                        }

                                        return new ExternalServiceOperationException(message);
                                    })
                    )
                    .bodyToFlux(
                            new ParameterizedTypeReference<ServerSentEvent<String>>() {
                            }
                    )
                    .onErrorMap(
                            WebClientRequestException.class,
                            e -> new UnreachableExternalServiceException("AI service unreachable")
                    );

    }

    /**
     * Attempts to send the summary of a conversation and the newest messages to add into the summary
     * @param chatContextDTO DTO containing both List of messages and the actual summary
     * @return String with the new summary done by the AI
     */
    public String sendSummaryToTheLLM(ChatContextDTO chatContextDTO) {

        try {
            logger.info("Sending the summary to the AI Service through the Feign client");
            return aiApiFeignClient.sendSummaryToTheLLM(chatContextDTO);

        } catch (RetryableException e) {
            logger.warn("Failed to reach the API AI Service when trying to summarize: {}", e.toString());
            throw new UnreachableExternalServiceException("External service is unreachable..");
        } catch (Exception e) {
            logger.warn("Unexpected error response from the API AI Service trying to summarize with returning exception: {}", e.toString());
            throw new ExternalServiceOperationException("Unexpected external service error");
        }

    }

    /**
     * Retrieves the active model limits and static prompt token counts from the Python AI service.
     *
     * @return capabilities used by the backend to calculate the available context budget
     */
    public ChatModelCapabilities getModelCapabilities() {

        try {
            logger.info("Requesting model capabilities from the AI Service through the Feign client");
            return capabilitiesFeignClient.getCapabilities();

        } catch (RetryableException e) {
            logger.warn("Failed to reach the AI Service while retrieving model capabilities: {}", e.toString());
            throw new UnreachableExternalServiceException("External service is unreachable..");
        } catch (Exception e) {
            logger.warn("Unexpected error response from the AI Service while retrieving model capabilities: {}", e.toString());
            throw new ExternalServiceOperationException("Unexpected external service error");
        }
    }

    /**
     * Retrieves the configured embedding model and its vector dimensions from the Python AI service.
     *
     * @return embedding capabilities used by the backend to validate and interpret embedding vectors
     */
    public EmbeddingCapabilities getEmbeddingCapabilities() {

        try {
            logger.info("Requesting embedding capabilities from the AI Service through the Feign client");
            return capabilitiesFeignClient.getEmbeddingCapabilities();

        } catch (RetryableException e) {
            logger.warn("Failed to reach the AI Service while retrieving embedding capabilities: {}", e.toString());
            throw new UnreachableExternalServiceException("External service is unreachable..");
        } catch (Exception e) {
            logger.warn("Unexpected error response from the AI Service while retrieving embedding capabilities: {}", e.toString());
            throw new ExternalServiceOperationException("Unexpected external service error");
        }
    }

    /**
     * Sends message content to the Python AI service to generate its embedding chunks.
     * @param embeddingRequestDTO request containing the content to split and embed
     * @return response containing the chunks and their embedding metadata
     */
    public EmbeddingResponseDTO embedMessageByChunks(EmbeddingRequestDTO embeddingRequestDTO) {

        try {
            logger.info("Sending message content to the AI Service to generate embeddings");
            return embeddingApiFeignClient.embedMessageByChunks(embeddingRequestDTO);

        } catch (RetryableException e) {
            logger.warn("Failed to reach the AI Service while generating embeddings: {}", e.toString());
            throw new UnreachableExternalServiceException("External service is unreachable..");
        } catch (Exception e) {
            logger.warn("Unexpected error response from the AI Service during embedding generation: {}", e.toString());
            throw new ExternalServiceOperationException("Unexpected external service error");
        }
    }

    /**
     * Sends the complete user query to the Python AI service to generate one embedding for semantic search.
     * The query is embedded as a whole and is not split into chunks.
     *
     * @param query the full user query text to embed
     * @return the query vector and the embedding model used by the AI service
     */
    public QueryEmbeddingResponseDTO embedQuery(String query) {

        try {
            logger.info("Sending the complete query to the AI Service to generate its embedding");
            return embeddingApiFeignClient.embedQuery(query);

        } catch (RetryableException e) {
            logger.warn("Failed to reach the AI Service while generating a query embedding: {}", e.toString());
            throw new UnreachableExternalServiceException("External service is unreachable..");
        } catch (Exception e) {
            logger.warn("Unexpected error response from the AI Service during query embedding generation: {}", e.toString());
            throw new ExternalServiceOperationException("Unexpected external service error");
        }
    }

}
