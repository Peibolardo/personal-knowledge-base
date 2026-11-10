package backend.repository;

import backend.model.MessageChunk;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageChunkRepository extends JpaRepository<MessageChunk, String> {

    @Query("SELECT mc from MessageChunk INNER JOIN message m on m.id = mc.messageId WHERE m.conversation_id =: conversationId")
    public List<MessageChunk> getChunksByConversationAndSimilarity(@Value("similarity")Double similarity, @Value("conversationId") String conversationId);

}
