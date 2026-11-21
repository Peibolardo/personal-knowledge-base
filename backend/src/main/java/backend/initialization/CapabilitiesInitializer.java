package backend.initialization;

import backend.dto.capabilities.ModelCapabilities;
import backend.provider.ModelCapabilitiesProvider;
import backend.utils.externalServiceServices.ExternalAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Loads the AI service's model capabilities into the application during startup.
 *
 * <p>Spring Boot runs this component because it implements {@link ApplicationRunner}. It retrieves
 * the capabilities and stores them in {@link ModelCapabilitiesProvider} before startup is considered
 * complete, so the backend can use the model's token limits and prompt counts.</p>
 */
@Component
@RequiredArgsConstructor
public class CapabilitiesInitializer implements ApplicationRunner {

    private final ExternalAiService externalAiService;
    private final ModelCapabilitiesProvider modelCapabilitiesProvider;

    /**
     * Retrieves the current capabilities from the AI service and stores them in the provider.
     *
     * <p>Spring Boot invokes {@code run} once during startup, after the application context has been
     * prepared. The supplied arguments are available for startup-specific behavior, though this
     * initializer does not currently need them.</p>
     *
     * @param args application arguments passed to Spring Boot
     * @throws Exception if retrieving or initializing the capabilities fails, preventing startup
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {

        ModelCapabilities modelCapabilities = externalAiService.getModelCapabilities();
        modelCapabilitiesProvider.initialize(modelCapabilities);

    }
}
