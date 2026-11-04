package backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object for embedding responses, containing the generated chunks.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmbeddingResponseDTO {

    @Valid
    @NotEmpty(message = "Chunks cannot be empty")
    private List<EmbeddingChunkDTO> chunks;

}
