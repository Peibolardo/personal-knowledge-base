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
public class ChatStreamErrorDTO {

    @NotBlank(message = "Error code cannot be null")
    private String code;

    @NotBlank(message = "Error message cannot be null")
    private String message;

    private boolean retryable;

}
