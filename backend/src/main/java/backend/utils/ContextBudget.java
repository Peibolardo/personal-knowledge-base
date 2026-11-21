package backend.utils;

import backend.config.GlobalContextConfig;
import backend.provider.ModelCapabilitiesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ContextBudget {

    private static final Logger logger = LoggerFactory.getLogger(ContextBudget.class);
    private final ModelCapabilitiesProvider modelCapabilitiesProvider;
    private final GlobalContextConfig globalContextConfig;

    public ContextBudget(ModelCapabilitiesProvider modelCapabilitiesProvider, GlobalContextConfig globalContextConfig) {
        this.modelCapabilitiesProvider = modelCapabilitiesProvider;
        this.globalContextConfig = globalContextConfig;
    }


    private int getChatRequestBudget(){

        int contextWindowTokens = modelCapabilitiesProvider.getCapabilities().getContextWindowTokens();
        int modelMaxCompletionTokens = modelCapabilitiesProvider.getCapabilities().getModelMaxCompletionTokens();
        int

        logger.info("Retrieving the context maximum to use based on the Configurations and Model capabilities");


    }

    private int getSummaryRequestBudget(){}









}
