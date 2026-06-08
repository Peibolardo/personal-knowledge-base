package backend.service.command;

import backend.dto.ChatRequestDTO;
import backend.dto.ChatResponseDTO;
import backend.exception.customExceptions.ConflictException;
import backend.service.interfaces.OpenAICommandService;
import backend.utils.externalServiceServices.ExternalAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OpenAICommandControllerImpl implements OpenAICommandService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAICommandControllerImpl.class);
    private final ExternalAiService externalAiService;

    public OpenAICommandControllerImpl(ExternalAiService externalAiService) {
        this.externalAiService = externalAiService;
    }

    public ChatResponseDTO sendMessageToApi(ChatRequestDTO chatRequestDTO){

        // 1.Send the message to the API and try to receive the response
        ChatResponseDTO responseDTO = externalAiService.sendMessageToAi(chatRequestDTO);

        // 2.Check if the response exists
        if(responseDTO == null){
            throw new ConflictException("There was no response for this request");
        }

        return responseDTO;

    }



}
