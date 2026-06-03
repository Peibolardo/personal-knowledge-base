package backend.exception.customExceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a client's request is syntactically correct but logically invalid
 * according to business rules. For example, attempting an invalid status transition
 * or providing invalid data in a request body that passes basic validation.
 *
 * This results in an HTTP 400 Bad Request response.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}