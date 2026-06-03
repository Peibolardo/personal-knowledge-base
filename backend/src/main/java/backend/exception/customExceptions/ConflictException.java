package backend.exception.customExceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a request could not be completed due to a conflict with the current
 * state of the target resource. This is typically used when a user tries to create
 * a resource that already exists.
 *
 * This results in an HTTP 409 Conflict response.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}