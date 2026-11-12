package backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object for a complete query embedding and its model metadata.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueryEmbeddingResponseDTO {

    @NotEmpty(message = "Embedding cannot be empty")
    @Size(min = 1536, max = 1536, message = "Embedding must contain exactly 1536 dimensions")
    private List<Float> embedding;

    @JsonProperty("embedding_model")
    @NotBlank(message = "Embedding model cannot be blank")
    private String embeddingModel;

}
