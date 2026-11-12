package backend.service.interfaces;

import backend.model.Message;

import java.util.List;
import java.util.UUID;

public interface EmbeddingCommandService {

    /** Generates and persists message chunk embeddings for both messages in a conversation turn. */
    void embedRequestResponseMessages(String requestId, String responseId);

    /** Generates one full-message embedding and returns it in PostgreSQL vector literal format. */
    String embedMessageWithoutChunking(Message message);

    /** Retrieves one ranked page of unique turn IDs for the supplied query vector. */
    List<UUID> semanticSearchPagination(
            String queryVector,
            String conversationId,
            String embeddingModel,
            int offset,
            int batchSize
    );

}
