package backend.exception.customExceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when the server got an invalid response while working as a gateway to get the response needed to handle the request.
 * Results in an HTTP 502 Bad Gateway.
 */
@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class ExternalServiceOperationException extends RuntimeException {
    public ExternalServiceOperationException(String message) {
        super(message);
    }
}