package backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for embedding requests, containing the content to split into chunks and embed.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmbeddingRequestDTO {

    @NotBlank(message = "Content cannot be blank")
    private String content;

}
