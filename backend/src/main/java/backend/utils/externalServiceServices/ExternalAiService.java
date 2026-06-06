package backend.utils.externalServiceServices;

import backend.dto.ChatRequestDTO;
import backend.dto.ChatResponseDTO;
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

    public ChatResponseDTO sendMessageToAi(ChatRequestDTO chatRequestDTO){

        

    }

}
