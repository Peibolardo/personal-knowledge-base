package backend.exception;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.UUID;


/**
 * A servlet filter that intercepts every incoming HTTP request to establish a Correlation ID.
 *
 * This filter ensures that every request has a unique ID for tracing purposes. The flow is as follows:
 * 1. It checks if the incoming request already has an {@code X-Correlation-ID} header. If so, it uses it.
 * This allows upstream services or clients to propagate a trace ID.
 * 2. If the header is not present, a new unique ID (UUID) is generated.
 * 3. The ID is placed into the SLF4J Mapped Diagnostic Context (MDC), making it automatically
 * available to the logging framework for every log statement made during the request's lifecycle.
 * 4. The ID is also added as a header to the HTTP response, allowing the client to receive it for
 * their own logging or for displaying to a user in case of an error.
 *
 * This class is annotated with @Component to be automatically detected and registered by Spring Boot.
 */
@Component
@org.springframework.core.annotation.Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE)//For high execution priority.
public class CorrelationIdFilter extends OncePerRequestFilter {

    /** The default HTTP header name for the correlation ID. */
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    /** The key used to store the correlation ID in the SLF4J MDC. */
    private static final String CORRELATION_ID_KEY = "correlationId";

    /** The requesting userId included in the transaction*/
    private static final String USER_ID_HEADER = "userId";

    /** The key you defined in logback.xml */
    private static final String USER_ID_KEY = "userId";

    /**
     * Intercepts the request to manage the correlation ID.
     *
     * @param request The incoming HTTP request.
     * @param response The outgoing HTTP response.
     * @param filterChain The chain of filters to proceed with the request.
     * @throws ServletException If a servlet-specific error occurs.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // 1. Handle Correlation ID
            String correlationId = request.getHeader(CORRELATION_ID_HEADER);
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString();
            }
            MDC.put(CORRELATION_ID_KEY, correlationId);
            response.addHeader(CORRELATION_ID_HEADER, correlationId);

            // 2. Handle User ID
            String userId = request.getHeader(USER_ID_HEADER);
            if (userId != null && !userId.isBlank()) {
                MDC.put(USER_ID_KEY, userId);
            }

            filterChain.doFilter(request, response);

        } finally {
            // 3. Clean up ALL keys
            MDC.remove(CORRELATION_ID_KEY);
            MDC.remove(USER_ID_KEY);
        }
    }

}