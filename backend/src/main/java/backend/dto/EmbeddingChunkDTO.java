package backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object for one content chunk and its generated embedding metadata.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmbeddingChunkDTO {

    @NotBlank(message = "Content cannot be blank")
    private String content;

    @JsonProperty("chunk_index")
    @NotNull(message = "Chunk index cannot be null")
    @PositiveOrZero(message = "Chunk index cannot be negative")
    private Integer chunkIndex;

    @JsonProperty("token_count")
    @NotNull(message = "Token count cannot be null")
    @PositiveOrZero(message = "Token count cannot be negative")
    private Integer tokenCount;

    @JsonProperty("embedding")
    @NotEmpty(message = "Embedding cannot be empty")
    @Size(min = 1536, max = 1536, message = "Embedding must contain exactly 1536 dimensions")
    private List<Float> embedding;

    @JsonProperty("embedding_model")
    @NotBlank(message = "Embedding model cannot be blank")
    private String embeddingModel;

}
