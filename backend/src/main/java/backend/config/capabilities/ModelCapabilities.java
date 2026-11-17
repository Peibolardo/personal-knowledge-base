package backend.config.capabilities;

import lombok.Getter;
import lombok.Setter;

/**
 * Model capabilities class to store the limitations of the actual model used in AI Service
 */
@Getter
@Setter
public class ModelCapabilities {

    /**
     * The actual model
     */
    private String model;

    /**
     * Context window limit
     */
    private int contextWindowTokens;

    /**
     * Max completion tokens limit
     */
    private int modelMaxCompletionTokens;

    /**
     * Contains the prompt token usage of the different call types to the AI Service
     */
    private PromptCapabilities prompts;


}
