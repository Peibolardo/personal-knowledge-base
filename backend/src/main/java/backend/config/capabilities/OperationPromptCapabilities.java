package backend.config.capabilities;

import lombok.Getter;
import lombok.Setter;

/**
 * Capabilities class to store the system prompts used and the developer prompts tokens used
 */
@Getter
@Setter
public class OperationPromptCapabilities {

    private int systemPromptTokens;

    private int developerPromptTokens;

}
