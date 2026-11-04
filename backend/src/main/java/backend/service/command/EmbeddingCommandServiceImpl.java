package backend.service.command;

import backend.repository.MessageChunkRepository;
import backend.service.interfaces.EmbeddingCommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmbeddingCommandServiceImpl implements EmbeddingCommandService{

    private static final Logger logger = LoggerFactory.getLogger(EmbeddingCommandServiceImpl.class);

    private final MessageChunkRepository messageChunkRepository;

    public EmbeddingCommandServiceImpl(MessageChunkRepository messageChunkRepository) {
        this.messageChunkRepository = messageChunkRepository;
    }




}
