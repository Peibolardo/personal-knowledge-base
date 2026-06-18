package backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("tokens_prompt")
    private Integer tokensPrompt;

    @JsonProperty("tokens_completion")
    private Integer tokensCompletion;

    @JsonProperty("tokens_total")
    private Integer tokensTotal;

    @JsonProperty("model_used")
    @NotBlank(message = "Model can not be blank")
    @Size(max = 255, message = "Model must not exceed 255 characters")
    private String model;

    private String conversationId;

}
