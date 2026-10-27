package backend.sse.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * Data Transfer Object for Error Event Type for the SSE architecture.
 * Contains the information from the OpenAI Stream response in case of error.
 */
public class ChatStreamErrorDTO {

    @NotBlank(message = "Error code cannot be null")
    private String code;

    @NotBlank(message = "Error message cannot be null")
    private String message;

}
