package backend.config;

import backend.dto.capabilities.ChatModelCapabilities;
import lombok.Getter;
import lombok.Setter;
//Binds application.properties to our Config attributes looking for the prefix
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Class to set up the Global Context Budget Management
 * with the objective to establish a global token budget accounting for all prompt components, the model's context window and reserved output tokens.
 */
@Configuration
@ConfigurationProperties(prefix = "ai.context")
@Getter
@Setter
public class GlobalContextConfig {

    /**
     * Maximum tokens allocated to request message
     */
    private int requestMessageTokenBudget;

    /**
     * Maximum tokens allocated to recent messages, excluding the request message
     */
    private int recentTokenBudget;

    /**
     * 	Maximum tokens allocated to retrieved history
     */
    private int semanticTokenBudget;

    /**
     * 	Maximum completion tokens reserved for chat completions/requestAPI response
     */
    private int chatTokenBudget;

    /**
     * 	Maximum tokens generated when creating/updating the summary
     */
    private int summaryTokenBudget;

    /**
     * Unsummarized tokens needed to trigger a summary update
     */
    private int summarizationTrigger;

    /**
     * Extra reserve to avoid exceeding the model's context window
     */
    private int inputSafetyMargin;

    /**
     * Helper function to retrieve the available context window tokens for chat
     */
    public int getAvailableChatInputTokens(ChatModelCapabilities capabilities) {

        return capabilities.getContextWindowTokens()
                - requestMessageTokenBudget
                - chatTokenBudget
                - summaryTokenBudget
                - inputSafetyMargin;
    }

    /**
     * Helper function to retrieve the available context window tokens for summarization
     */
    public int getAvailableSummaryInputTokens(ChatModelCapabilities capabilities) {

        return capabilities.getContextWindowTokens()
                - summaryTokenBudget
                - inputSafetyMargin;
    }
}
