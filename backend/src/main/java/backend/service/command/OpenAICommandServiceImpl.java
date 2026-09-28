package backend.service.command;

import backend.dto.ChatRequestDTO;
import backend.dto.ChatResponseDTO;
import backend.exception.customExceptions.ConflictException;
import backend.repository.MessageRepository;
import backend.service.interfaces.OpenAICommandService;
import backend.utils.externalServiceServices.ExternalAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OpenAICommandServiceImpl implements OpenAICommandService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAICommandServiceImpl.class);
    private final MessageRepository messageRepository;
    private final ExternalAiService externalAiService;

    public OpenAICommandServiceImpl(MessageRepository messageRepository, ExternalAiService externalAiService) {
        this.messageRepository = messageRepository;
        this.externalAiService = externalAiService;
    }

    public ChatResponseDTO sendMessageToApi(ChatRequestDTO chatRequestDTO){

        // 1. Check if the request has conversationId


        // 1.Send the message to the API and try to receive the response
        ChatResponseDTO responseDTO = externalAiService.sendMessageToAi(chatRequestDTO);

        // 2.Check if the response exists
        if(responseDTO == null){
            throw new ConflictException("There was no response for this request");
        }

        return responseDTO;

    }



}
