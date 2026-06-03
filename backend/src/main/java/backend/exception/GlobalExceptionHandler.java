package backend.exception;

import backend.exception.customExceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;


/**
 * A centralized exception handler for the entire microservice.
 * This class uses @ControllerAdvice to intercept exceptions.
 *
 * Its primary goals are:
 * 1. To provide standardized, RFC 7807 compliant error responses using {@link ProblemDetail}.
 * 2. To enhance security by preventing information leakage. It logs detailed error information
 * and sends it to the SystemLogs but returns generic, safe messages to the client.
 * 3. To improve traceability and debugging by ensuring every error response includes a
 * correlation ID, linking it to the server logs.
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** The key used to store and retrieve the correlation ID from the SLF4J Mapped Diagnostic Context (MDC). */
    private static final String CORRELATION_ID_KEY = "correlationId";

    /**
     * Handles specific, anticipated application exceptions such as {@link ResourceNotFoundException}
     *
     * It uses the @ResponseStatus annotation on the exception class to determine the appropriate HTTP status code.
     * This handler ensures that while the specific exception message is logged for debugging, the client
     * receives a generic, non-revealing error detail.
     *
     * @param ex The runtime exception caught.
     * @param request The current web request.
     * @return A ResponseEntity containing a standardized {@link ProblemDetail} body.
     */
    @ExceptionHandler({ ResourceNotFoundException.class,
            InvalidRequestException.class,
            ConflictException.class,
            ExternalServiceOperationException.class,
            UnreachableExternalServiceException.class
    })
    public ResponseEntity<Object> handleCustomExceptions(RuntimeException ex, WebRequest request) {
        // Get the status from the exception's @ResponseStatus annotation
        ResponseStatus responseStatus = ex.getClass().getAnnotation(ResponseStatus.class);
        HttpStatus status = responseStatus != null ? responseStatus.value() : HttpStatus.INTERNAL_SERVER_ERROR;

        logger.warn("Handled application exception: status={}, message='{}'", status, ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, "An expected error has been identified");
        problemDetail.setTitle(status.getReasonPhrase());
        problemDetail.setInstance(URI.create(request.getDescription(false)));

        problemDetail.setProperty(CORRELATION_ID_KEY, MDC.get(CORRELATION_ID_KEY));

        logger.warn("Safely managed exception [{}]: status={}, message='{}'",
                MDC.get(CORRELATION_ID_KEY), status, ex.getMessage());

        //ToDo: Send a request ot the SystemLogs service to save the exception.

        return new ResponseEntity<>(problemDetail, status);
    }


    /**
     * Customizes the response for bean validation errors triggered by the @Valid annotation.
     *
     * This method overrides the default Spring handler to provide a consistent RFC 7807 response.
     * For security, it logs the detailed field-specific validation errors internally but only
     * returns a generic "Validation Failed" message to the client, preventing the exposure
     * of internal model field names and validation logic.
     *
     * @param ex The validation exception.
     * @param headers The HTTP headers.
     * @param status The HTTP status code determined by Spring.
     * @param request The current web request.
     * @return A ResponseEntity containing a standardized {@link ProblemDetail} body.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setTitle("Validation Failed");
        problemDetail.setDetail("One or more fields are invalid.");

        problemDetail.setInstance(URI.create(request.getDescription(false)));
        problemDetail.setProperty(CORRELATION_ID_KEY, MDC.get(CORRELATION_ID_KEY));

        //ToDo: Send a request ot the SystemLogs service to save the exception.
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());
        logger.warn("Validation failed [{}]: {}", MDC.get(CORRELATION_ID_KEY), errors);

        return new ResponseEntity<>(problemDetail, status);
    }

    /**
     * A fallback handler for any other unhandled {@link Exception}.
     *
     * This method acts as a safety net, ensuring that the application never leaks raw stack traces
     * or default server error pages to the client. It catches any unexpected exception, logs the
     * full stack trace for debugging, and returns a generic HTTP 500 "Internal Server Error"
     * response, protecting internal implementation details.
     *
     * @param ex The unexpected exception caught.
     * @param request The current web request.
     * @return A ResponseEntity containing a generic but structured HTTP 500 {@link ProblemDetail}.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleAllUncaughtException(Exception ex, WebRequest request) {

        String correlationId = MDC.get(CORRELATION_ID_KEY);
        //ToDo: Send a request ot the SystemLogs service to save the exception.
        logger.error("An unexpected internal error occurred [{}]: ", correlationId, ex);

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setDetail("An unexpected internal error occurred.");
        problemDetail.setInstance(URI.create(request.getDescription(false)));
        problemDetail.setProperty(CORRELATION_ID_KEY, correlationId);

        return new ResponseEntity<>(problemDetail, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}