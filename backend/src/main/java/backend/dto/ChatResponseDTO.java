package backend.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Data Transfer Object for Chat Response resources, used to receive chat responses information from endpoints
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponseDTO {

    @NotBlank(message = "response can not be blank")
    private String response;

    private Integer tokensPrompt;

    private Integer tokensCompletion;

    private Integer tokensTotal;

    @NotBlank(message = "Model can not be blank")
    @Size(max = 255, message = "Model must not exceed 255 characters")
    private String model;

}
