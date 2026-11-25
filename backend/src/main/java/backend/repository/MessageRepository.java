package backend.repository;

import backend.model.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {

    /**
     * //TODO add user as condition when auth is added is Phase5
     * Gets the last 9 messages from a conversation to retrieve the context.
     * @param conversationId The identifier of the conversation.
     * @return List<Message>
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId ORDER BY creationDate DESC")
    List<Message> getContextMessages(@Param("conversationId") String conversationId, Pageable pageable);

    /**
     * Gets every message belonging to a turn in the specified conversation, newest first.
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.turnId = :turnId ORDER BY m.creationDate DESC")
    List<Message> getTurnMessages(@Param("conversationId") String conversationId, @Param("turnId") java.util.UUID turnId);

    /**
     * Loads all messages for a batch of turns in one query, newest first within each turn.
     *
     * @param conversationId conversation that owns the requested turns
     * @param turnIds IDs of the turns to load
     * @return messages belonging to the requested turns
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.turnId IN :turnIds ORDER BY m.creationDate DESC")
    List<Message> getTurnMessagesForTurns(
            @Param("conversationId") String conversationId,
            @Param("turnIds") List<UUID> turnIds
    );

    /**
     * Gets all the messages from a specific conversation
     * @param conversationId The identifier of the conversation.
     * @return List<Message>
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId ORDER BY creationDate ASC")
    List<Message> getConversationMessages(@Param("conversationId") String conversationId);

}
