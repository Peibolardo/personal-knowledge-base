package backend.service.interfaces;

import backend.dto.ChatRequestDTO;
import backend.dto.ChatResponseDTO;
import backend.exception.customExceptions.ExternalServiceOperationException;
import backend.exception.customExceptions.InvalidRequestException;
import backend.exception.customExceptions.UnreachableExternalServiceException;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

public interface OpenAICommandService {

    /**
     * Function to send the context window of 10 messages max to the AI-API order from oldest to newest
     * @param chatRequestDTO The new message sent by the user
     * @return ChatResponseDTO with the AI response.
     *
     * @throws InvalidRequestException if input IDs or DTO data are invalid.
     * @throws UnreachableExternalServiceException If the API Service is not online
     * @throws ExternalServiceOperationException If an exception occurs in the API Service
     */
    ChatResponseDTO sendMessageToApi(ChatRequestDTO chatRequestDTO);

    /**
     * Sends a chat request to the AI service and returns its response as an SSE stream.
     *
     * Execution flow:
     * 1. Prepare the conversation context and the user message before making the AI request.
     * 2. Use Flux.defer() to delay the AI request and stream initialization until subscription.
     *    A new StreamState is created for each subscription.
     * 3. Use concatMap() to process incoming SSE events sequentially through handleEvent().
     * 4. Use onErrorResume() to distinguish between errors before the first emitted event
     *    and errors occurring after streaming has started. Before the first event, propagate
     *    the error; afterwards, emit an SSE error event.
     * 5. Use doOnNext() to track whether an event has been emitted and whether a terminal
     *    event (done/error) has been received.
     * 6. Use takeUntil() to forward the terminal event and then stop the upstream stream.
     * 7. Use concatWith() to check whether the stream ended without a terminal event.
     *    If so, emit a fallback SSE error event; otherwise, complete without emitting anything.
     *
     * Important:
     * - prepareContext() executes before the deferred publisher is subscribed to.
     * - started tracks event emission, not whether the HTTP response has been committed.
     * - concatMap() preserves sequential event processing, which is necessary to accumulate
     *   the assistant's response before persisting it when the done event arrives.
     *
     * @param chatRequestDTO the user's chat request
     * @return a Flux of Server-Sent Events to be forwarded to the client
     */
     Flux<ServerSentEvent<String>> sendMessageToApiStreaming(ChatRequestDTO chatRequestDTO);
}
