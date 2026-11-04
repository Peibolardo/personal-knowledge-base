package backend.repository;

import backend.model.MessageChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageChunkRepository extends JpaRepository<MessageChunk, String> {



}
