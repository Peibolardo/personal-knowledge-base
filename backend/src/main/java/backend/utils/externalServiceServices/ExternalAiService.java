package backend.utils.externalServiceServices;

import backend.dto.ChatRequestDTO;
import backend.dto.ChatResponseDTO;
import backend.exception.customExceptions.ExternalServiceOperationException;
import backend.exception.customExceptions.UnreachableExternalServiceException;
import feign.RetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service

public class ExternalAiService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalAiService.class);
    private final AiApiFeignClient aiApiFeignClient;

    public ExternalAiService(AiApiFeignClient aiApiFeignClient) {
        this.aiApiFeignClient = aiApiFeignClient;
    }

    /**
     * Attempting to send the message to the Api for the AI
     * @param chatRequestDTO The DTO with the message information
     * @return The response from the AI
     */
    public ChatResponseDTO sendMessageToAi(ChatRequestDTO chatRequestDTO){

        logger.info("Attempting to send a message to the Python service");

        try{
            return aiApiFeignClient.sendMessageToAi(chatRequestDTO);
        }
        catch(RetryableException e){
            logger.warn("Failed to reach the Mission Service when fetching the running missions with retuning exception: {}", e.toString());
            throw new UnreachableExternalServiceException("External service is unreachable..");
        }
         catch (Exception e) {
            logger.warn("Unexpected error response from the Mission Service when fetching the running missions with retuning exception: {}", e.toString());
            throw new ExternalServiceOperationException("Unexpected external service error");
        }
    }

}
