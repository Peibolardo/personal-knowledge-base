package backend.provider;

import backend.dto.capabilities.ChatModelCapabilities;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * Bean class to store and provide the capabilities set in the application initialization
 */
@Component
@Getter
public class ChatModelCapabilitiesProvider {

    private ChatModelCapabilities capabilities;

    public void initialize(ChatModelCapabilities capabilities) {
        this.capabilities = capabilities;
    }

}
