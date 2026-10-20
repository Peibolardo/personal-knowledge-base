package backend.exception.customExceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when the server got an invalid deserialized object coming from the AI Service.
 * Results in an HTTP 406 Not acceptable.
 */
@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class IllegalObjectDeserializedException extends Exception{

    public IllegalObjectDeserializedException(String message) {
        super(message);
    }

}
