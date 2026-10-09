package backend.sse.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * Data Transfer Object for Done Event Type for the SSE architecture.
 * Contains information regarding the token used and marks the end of the Stream response. */
public class ChatStreamDoneDTO {

    @JsonProperty("tokens_prompt")
    private Integer tokensPrompt;

    @JsonProperty("tokens_completion")
    private Integer tokensCompletion;

    @JsonProperty("tokens_total")
    private Integer tokensTotal;

}
