package backend.sse.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Metadata Event Type for the SSE architecture.
 * Contains the basic information from the OpenAI Stream response.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatStreamMetadataDTO {

    @NotBlank(message = "conversationId cannot be blank")
    private String conversationId;

    //Property that return the token cost of the last message sent
    @JsonProperty("tokens_input_message")
    private Integer tokensInputMessage;

    @JsonProperty("model_used")
    @NotBlank(message = "Model cannot be blank")
    @Size(max = 255, message = "Model must not exceed 255 characters")
    private String model;



}
