package backend.service.command;

import backend.exception.customExceptions.ResourceNotFoundException;
import backend.model.Message;
import backend.repository.MessageChunkRepository;
import backend.repository.MessageRepository;
import backend.service.interfaces.EmbeddingCommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static backend.utils.IdValidator.validateUuids;

@Service
public class EmbeddingCommandServiceImpl implements EmbeddingCommandService{

    private static final Logger logger = LoggerFactory.getLogger(EmbeddingCommandServiceImpl.class);

    private final MessageChunkRepository messageChunkRepository;
    private final MessageRepository messageRepository;

    public EmbeddingCommandServiceImpl(MessageChunkRepository messageChunkRepository, MessageRepository messageRepository) {
        this.messageChunkRepository = messageChunkRepository;
        this.messageRepository = messageRepository;
    }


    public void embedRequestResponseMessages(String requestId, String responseId){

        // 1. Validate both ids
        validateUuids(requestId, responseId);

        // 2. Retrieve both persisted messages
        Message requestMessage = messageRepository.findById(requestId).orElseThrow(
                () -> new ResourceNotFoundException("There is no request message with ID " + requestId)
        );
        Message responseMessage = messageRepository.findById(responseId).orElseThrow(
                () -> new ResourceNotFoundException("There is no response message with ID " + responseId)
        );


    }


}
