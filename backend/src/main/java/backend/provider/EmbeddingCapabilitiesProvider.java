package backend.provider;

import backend.dto.capabilities.EmbeddingCapabilities;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * Bean class to store and provide the embedding capabilities set in the application initialization
 */
@Component
@Getter
public class EmbeddingCapabilitiesProvider {

    private EmbeddingCapabilities embeddingCapabilities;

    public void initialize(EmbeddingCapabilities capabilities) {
        this.embeddingCapabilities = capabilities;
    }

}
