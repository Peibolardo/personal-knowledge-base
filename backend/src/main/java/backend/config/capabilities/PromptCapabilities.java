package backend.config.capabilities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PromptCapabilities {

    private OperationPromptCapabilities chat;

    private OperationPromptCapabilities summarization;

}
