package backend.utils;

import backend.exception.customExceptions.InvalidRequestException;
import backend.exception.customExceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class IdValidator {

    private static final Logger logger = LoggerFactory.getLogger(IdValidator.class);

    private IdValidator(){}

    /**
     * Validates that the provided string IDs are in a valid UUID format.
     *
     * @param ids A variable number of string IDs to validate.
     * @throws InvalidRequestException if any of the provided IDs is not a valid UUID format.
     */
    public static void validateUuids(String... ids) {
        for (String id : ids) {
            if (id != null) {
                try {
                    UUID.fromString(id);
                } catch (IllegalArgumentException e){
                    logger.warn("Invalid ID format detected: {}", id);
                    throw new InvalidRequestException("Invalid ID format provided: " + id);
                }
            }
        }
    }

    /**
     * Validates a UUID format and checks if a resource with that ID exists.
     *
     * @param id The ID to validate.
     * @param repository The repository to check for the existence of the resource.
     * @param resourceName The name of the resource (e.g., "Mission", "Layer").
     * @return The found entity.
     * @throws InvalidRequestException if the ID format is invalid.
     * @throws ResourceNotFoundException if the resource is not found.
     */
    public static <T, ID> T validateAndFind(String id, CrudRepository<T, ID> repository, String resourceName) {
        validateUuids(id);
        Optional<T> entityOptional = repository.findById((ID) id);
        return entityOptional.orElseThrow(() -> {
            logger.warn("Authorization failed: {} with ID {} not found.", resourceName, id);
            return new ResourceNotFoundException(resourceName + " with ID " + id + " not found.");
        });
    }
}