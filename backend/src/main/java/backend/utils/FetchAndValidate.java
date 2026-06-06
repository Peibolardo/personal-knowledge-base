package backend.utils;

import backend.exception.customExceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.CrudRepository;

public class FetchAndValidate {

    private static final Logger logger = LoggerFactory.getLogger(FetchAndValidate.class);

    private FetchAndValidate(){};

    /**
     * Performs a validation and retrieval of the given resource. This helper method
     * checks if an entity exists in the specified repository and throws a {@link ResourceNotFoundException}
     * if it doesn't.
     *
     * @param id The ID of the entity to be found.
     * @param repository The targeted instance of the repository where the query is performed.
     * @param resourceName The user-friendly name of the entity (e.g., "LayerType", "Picture") for error messages.
     * @param <T> The type of the entity to be retrieved.
     * @return The found entity. If the provided ID is null, it returns null without performing a lookup.
     * @throws ResourceNotFoundException if no entity with the given ID is found in the repository.
     */
    public static <T> T fetchAndValidate(String id, CrudRepository<T, String> repository, String resourceName) {
        if (id == null) {
            return null;
        }
        return repository.findById(id).orElseThrow(() -> {
            logger.warn("{} with ID {} not found.", resourceName, id);
            return new ResourceNotFoundException(resourceName + " with ID " + id + " not found.");
        });
    }
}
