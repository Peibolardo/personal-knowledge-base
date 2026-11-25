package backend.dto.capabilities;

import lombok.Getter;
import lombok.Setter;

/**
 * Embedding model capabilities class to store the limitations of the actual embedding model used in AI Service
 */
@Getter
@Setter
public class EmbeddingCapabilities {

    private String model;

    private int dimensions;

}
