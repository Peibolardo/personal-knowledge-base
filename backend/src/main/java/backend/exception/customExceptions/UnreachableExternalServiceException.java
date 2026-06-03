package backend.exception.customExceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when the server is acting as a gateway and cannot get a response in time for a request.
 * Results in an HTTP 503. Service Unavailable.
 */
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class UnreachableExternalServiceException extends RuntimeException {
    public UnreachableExternalServiceException(String message) {
        super(message);
    }
}