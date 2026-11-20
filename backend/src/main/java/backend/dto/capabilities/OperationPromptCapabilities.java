package backend.dto.capabilities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Capabilities class to store the system prompts used and the developer prompts tokens used
 */
@Getter
@Setter
public class OperationPromptCapabilities {

    @JsonProperty("system_prompt_tokens")
    private int systemPromptTokens;

    @JsonProperty("developer_prompt_tokens")
    private int developerPromptTokens;

}
