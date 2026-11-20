package backend.dto.capabilities;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("context_window_tokens")
    private int contextWindowTokens;

    /**
     * Max completion tokens limit
     */
    @JsonProperty("model_max_completions_tokens")
    private int modelMaxCompletionTokens;

    /**
     * Contains the prompt token usage of the different call types to the AI Service
     */
    private PromptCapabilities prompts;


}
