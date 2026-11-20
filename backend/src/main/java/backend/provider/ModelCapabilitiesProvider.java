package backend.provider;

import backend.dto.capabilities.ModelCapabilities;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * Bean class to store and provide the capabilities set in the application initialization
 */
@Component
@Getter
public class ModelCapabilitiesProvider {

    private ModelCapabilities capabilities;

    public void initialize(ModelCapabilities capabilities) {
        this.capabilities = capabilities;
    }

}
