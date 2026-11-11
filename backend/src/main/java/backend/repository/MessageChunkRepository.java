package backend.repository;

import backend.model.MessageChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageChunkRepository extends JpaRepository<MessageChunk, String> {

    /**
     * Retrieves unique conversation turn IDs ranked by the nearest matching chunk, using offset pagination.
     *
     * @param conversationId conversation whose chunks are searched
     * @param embeddingModel model that produced the query and stored vectors
     * @param queryEmbedding PostgreSQL vector literal used for cosine-distance comparison
     * @param maxDistance maximum accepted cosine distance
     * @param batchSize maximum number of unique turns returned
     * @param rowOffset number of unique turn candidates to skip
     * @return ranked turn IDs for the requested page
     */
    @Query(value = """
        SELECT m.turn_id
        FROM message_chunks mc
        INNER JOIN messages m ON m.id = mc.message_id
        WHERE m.conversation_id = :conversationId
          AND mc.embedding_model = :embeddingModel
          AND m.turn_id IS NOT NULL
          AND mc.embedding <=> CAST(:queryEmbedding AS vector) <= :maxDistance
        GROUP BY m.turn_id
        ORDER BY MIN(mc.embedding <=> CAST(:queryEmbedding AS vector)) ASC
        LIMIT :batchSize OFFSET :rowOffset
        """, nativeQuery = true)
    List<UUID> getTurnIdsByConversationAndSimilarity(
            @Param("conversationId") String conversationId,
            @Param("embeddingModel") String embeddingModel,
            @Param("queryEmbedding") String queryEmbedding,
            @Param("maxDistance") double maxDistance,
            @Param("batchSize") int batchSize,
            @Param("rowOffset") int rowOffset
    );

}
