package backend.repository;

import backend.model.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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
     * Gets all the messages from a specific conversation
     * @param conversationId The identifier of the conversation.
     * @return List<Message>
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId ORDER BY creationDate DESC")
    List<Message> getConversationMessages(@Param("conversationId") String conversationId);

}
