package backend.utils;

import backend.config.GlobalContextConfig;
import backend.dto.capabilities.ModelCapabilities;
import backend.provider.ModelCapabilitiesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Calculates the remaining dynamic input-token budget for chat and summarization.
 * The calculation subtracts reserved output/safety tokens and the static system and developer prompts
 * from the model's available context window.
 */
@Component
public class ContextBudget {

    private static final Logger logger = LoggerFactory.getLogger(ContextBudget.class);
    private final ModelCapabilities modelCapabilities;
    private final GlobalContextConfig globalContextConfig;

    /**
     * Creates the budget calculator using capabilities loaded at application startup and configured token reserves.
     *
     * @param modelCapabilitiesProvider provider initialized with the AI service's model and prompt capabilities
     * @param globalContextConfig configured output reserves, safety margin, and per-operation token limits
     */
    public ContextBudget(ModelCapabilitiesProvider modelCapabilitiesProvider, GlobalContextConfig globalContextConfig) {
        this.modelCapabilities = modelCapabilitiesProvider.getCapabilities();
        this.globalContextConfig = globalContextConfig;
    }

    /**
     * Calculates the dynamic token budget available for chat messages after fixed prompt and output reservations.
     *
     * @return remaining input tokens available for recent and semantically retrieved chat context
     * @throws IllegalStateException if configured reservations exceed the model's available input capacity
     */
    public int getChatDynamicContextBudget(){

        int availableTokens = globalContextConfig.getAvailableChatInputTokens(modelCapabilities);
        int developerTokens = modelCapabilities.getPrompts().getChat().getDeveloperPromptTokens();
        int systemTokens = modelCapabilities.getPrompts().getChat().getSystemPromptTokens();

        logger.info("Retrieving the maximum context for chat to use based on the Configurations and Model capabilities");

        return validateBudget(availableTokens - developerTokens - systemTokens, "chat");

    }

    /**
     * Calculates the dynamic token budget available for summarization input after fixed prompt and output reservations.
     *
     * @return remaining input tokens available for the current summary and messages being summarized
     * @throws IllegalStateException if configured reservations exceed the model's available input capacity
     */
    public int getSummaryDynamicContextBudget(){

        int availableTokens = globalContextConfig.getAvailableSummaryInputTokens(modelCapabilities);
        int developerTokens = modelCapabilities.getPrompts().getSummarization().getDeveloperPromptTokens();
        int systemTokens = modelCapabilities.getPrompts().getSummarization().getSystemPromptTokens();

        logger.info("Retrieving the maximum context for summarization to use based on the Configurations and Model capabilities");

        return validateBudget(availableTokens - developerTokens - systemTokens, "summarization");

    }

    /**
     * Ensures an operation's calculated context budget is non-negative before it is used.
     *
     * @param budget calculated remaining token count
     * @param operation operation name included in any configuration error
     * @return the validated budget
     * @throws IllegalStateException if the calculated budget is negative
     */
    private int validateBudget(int budget, String operation) {

        if (budget < 0) {
            throw new IllegalStateException(
                    "The available input budget for " + operation +
                            " is negative. Check model capabilities and context configuration."
            );
        }

        return budget;
    }


}
