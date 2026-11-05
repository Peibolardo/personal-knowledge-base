package backend.service.command;

import backend.dto.EmbeddingChunkDTO;
import backend.dto.EmbeddingRequestDTO;
import backend.dto.EmbeddingResponseDTO;
import backend.exception.customExceptions.ExternalServiceOperationException;
import backend.exception.customExceptions.InvalidRequestException;
import backend.exception.customExceptions.ResourceNotFoundException;
import backend.mapper.EmbeddingChunkMapper;
import backend.model.Message;
import backend.model.MessageChunk;
import backend.repository.MessageChunkRepository;
import backend.repository.MessageRepository;
import backend.service.interfaces.EmbeddingCommandService;
import backend.utils.externalServiceServices.ExternalAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static backend.utils.IdValidator.validateUuids;

@Service
public class EmbeddingCommandServiceImpl implements EmbeddingCommandService {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddingCommandServiceImpl.class);
    private static final int EMBEDDING_DIMENSIONS = 1536;

    private final MessageChunkRepository messageChunkRepository;
    private final MessageRepository messageRepository;
    private final ExternalAiService externalAiService;
    private final EmbeddingChunkMapper embeddingChunkMapper;

    public EmbeddingCommandServiceImpl(
            MessageChunkRepository messageChunkRepository,
            MessageRepository messageRepository,
            ExternalAiService externalAiService,
            EmbeddingChunkMapper embeddingChunkMapper
    ) {
        this.messageChunkRepository = messageChunkRepository;
        this.messageRepository = messageRepository;
        this.externalAiService = externalAiService;
        this.embeddingChunkMapper = embeddingChunkMapper;
    }

    /**
     * Loads and validates the two messages that make up a conversation turn, generates embeddings
     * for both messages, and persists their chunks only after both embedding responses are valid.
     *
     * @param requestId ID of the user message
     * @param responseId ID of the assistant response
     * @throws InvalidRequestException if the IDs do not identify the user and assistant messages from the same turn
     * @throws ResourceNotFoundException if either message cannot be found
     */
    public void embedRequestResponseMessages(String requestId, String responseId) {

        validateUuids(requestId, responseId);

        if (Objects.equals(requestId, responseId)) {
            throw new InvalidRequestException("Request and response message IDs must be different");
        }

        Message requestMessage = messageRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("There is no request message with ID " + requestId));

        Message responseMessage = messageRepository.findById(responseId)
                .orElseThrow(() -> new ResourceNotFoundException("There is no response message with ID " + responseId));

        if (!"user".equalsIgnoreCase(requestMessage.getRole())) {
            throw new InvalidRequestException("The request message must have the user role");
        }

        if (!"assistant".equalsIgnoreCase(responseMessage.getRole())) {
            throw new InvalidRequestException("The response message must have the assistant role");
        }

        if (requestMessage.getTurnId() == null
                || !requestMessage.getTurnId().equals(responseMessage.getTurnId())) {
            throw new InvalidRequestException("Request and response messages must belong to the same turn");
        }

        if (requestMessage.getConversation() == null || responseMessage.getConversation() == null
                || !Objects.equals(requestMessage.getConversation().getId(), responseMessage.getConversation().getId())) {
            throw new InvalidRequestException("Request and response messages must belong to the same conversation");
        }

        List<MessageChunk> chunksToPersist = new ArrayList<>();
        chunksToPersist.addAll(embedMessage(requestMessage));
        chunksToPersist.addAll(embedMessage(responseMessage));

        messageChunkRepository.saveAll(chunksToPersist);
        logger.info("Persisted {} embedding chunks for request {} and response {}", chunksToPersist.size(), requestId, responseId);
    }

    /**
     * Requests embeddings for one message, validates the returned chunks, and maps them to entities
     * associated with that message. The caller persists the mapped chunks after both turn messages succeed.
     *
     * @param message the user or assistant message to embed
     * @return validated embedding chunk entities associated with the message
     * @throws InvalidRequestException if the message has no content to embed
     * @throws ExternalServiceOperationException if the AI service returns an empty or malformed chunk response
     */
    private List<MessageChunk> embedMessage(Message message) {

        if (message.getContent() == null || message.getContent().isBlank()) {
            throw new InvalidRequestException("Message content cannot be blank when generating embeddings");
        }

        EmbeddingRequestDTO embeddingRequest = new EmbeddingRequestDTO(message.getContent());

        EmbeddingResponseDTO embeddingResponse = externalAiService.embedMessageByChunks(embeddingRequest);

        if (embeddingResponse == null || embeddingResponse.getChunks() == null || embeddingResponse.getChunks().isEmpty()) {
            throw new ExternalServiceOperationException("The AI service returned no embedding chunks");

        }

        List<MessageChunk> messageChunks = new ArrayList<>(embeddingResponse.getChunks().size());

        for (int index = 0; index < embeddingResponse.getChunks().size(); index++) {
            EmbeddingChunkDTO chunk = embeddingResponse.getChunks().get(index);

            if (chunk == null
                    || chunk.getChunkIndex() == null
                    || chunk.getChunkIndex() != index
                    || chunk.getContent() == null
                    || chunk.getContent().isBlank()
                    || chunk.getTokenCount() == null
                    || chunk.getTokenCount() <= 0
                    || chunk.getEmbedding() == null
                    || chunk.getEmbedding().size() != EMBEDDING_DIMENSIONS
                    || chunk.getEmbedding().stream().anyMatch(value -> value == null || !Float.isFinite(value))
                    || chunk.getEmbeddingModel() == null
                    || chunk.getEmbeddingModel().isBlank()) {
                throw new ExternalServiceOperationException(
                        "The AI service returned an invalid embedding chunk at index " + index
                );
            }

            MessageChunk messageChunk = embeddingChunkMapper.toEntity(chunk);
            messageChunk.setMessage(message);
            messageChunks.add(messageChunk);
        }

        return messageChunks;
    }
}
