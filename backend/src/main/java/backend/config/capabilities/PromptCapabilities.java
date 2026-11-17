package backend.config.capabilities;

import lombok.Getter;
import lombok.Setter;

/**
 * Capability class to receive the prompt and develop tokens for different chat completion and summarization calls
 */
@Getter
@Setter
public class PromptCapabilities {

    private OperationPromptCapabilities chat;

    private OperationPromptCapabilities summarization;

}
