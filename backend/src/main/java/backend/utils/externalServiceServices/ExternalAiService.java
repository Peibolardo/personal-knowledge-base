package backend.utils.externalServiceServices;

import backend.dto.ChatResponseDTO;
import backend.dto.ConversationSummaryDTO;
import backend.dto.MessageDTO;
import backend.exception.customExceptions.ExternalServiceOperationException;
import backend.exception.customExceptions.UnreachableExternalServiceException;
import feign.RetryableException;
import org.mapstruct.Qualifier;
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
    private final WebClient aiWebClient;

    public ExternalAiService(AiApiFeignClient aiApiFeignClient, WebClient aiWebClient) {
        this.aiApiFeignClient = aiApiFeignClient;
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
     * @param messageDTOList the conversation messages sent to the AI service
     * @return a Flux emitting SSE events as they arrive from the AI service
     */
    public Flux<ServerSentEvent<String>> streamingMessageToAi(List<MessageDTO> messageDTOList){

            return aiWebClient.post()
                    .uri("/chat/stream")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .bodyValue(messageDTOList)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp ->
                            resp.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .map(body -> new ExternalServiceOperationException(
                                            "AI service error " + resp.statusCode().value() + ": " + body)))
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
     * @param conversationSummaryDTO DTO containing both List of messages and the actual summary
     * @return String with the new summary done by the AI
     */
    public String sendSummaryToTheLLM(ConversationSummaryDTO conversationSummaryDTO){

        try{
            logger.info("Sending the summary to the AI Service through the Feign client");
            return aiApiFeignClient.sendSummaryToTheLLM(conversationSummaryDTO);

        }
        catch(RetryableException e){
            logger.warn("Failed to reach the API AI Service when trying to summarize: {}", e.toString());
            throw new UnreachableExternalServiceException("External service is unreachable..");
        }
        catch (Exception e) {
            logger.warn("Unexpected error response from the API AI Service trying to summarize with returning exception: {}", e.toString());
            throw new ExternalServiceOperationException("Unexpected external service error");
        }


    }


}
