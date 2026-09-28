package backend.utils.externalServiceServices;

import backend.dto.ChatRequestDTO;
import backend.dto.ChatResponseDTO;
import backend.dto.MessageDTO;
import backend.exception.customExceptions.ExternalServiceOperationException;
import backend.exception.customExceptions.UnreachableExternalServiceException;
import feign.RetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

public class ExternalAiService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalAiService.class);
    private final AiApiFeignClient aiApiFeignClient;

    public ExternalAiService(AiApiFeignClient aiApiFeignClient) {
        this.aiApiFeignClient = aiApiFeignClient;
    }

    /**
     * Attempting to send the message to the Api for the AI
     * @param messageDTOList The DTO list with the context information
     * @return The response from the AI
     */
    public ChatResponseDTO sendMessageToAi(List<MessageDTO> messageDTOList){

        logger.info("Attempting to send a message to the Python service");

        try{
            return aiApiFeignClient.sendMessageToAi(messageDTOList);
        }
        catch(RetryableException e){
            logger.warn("Failed to reach the API AI Service when trying to access OpenAI API: {}", e.toString());
            throw new UnreachableExternalServiceException("External service is unreachable..");
        }
         catch (Exception e) {
            logger.warn("Unexpected error response from the API AI Service when trying to access OpenAI API with returning exception: {}", e.toString());
            throw new ExternalServiceOperationException("Unexpected external service error");
        }
    }

}
