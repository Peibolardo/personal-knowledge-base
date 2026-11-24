import asyncio
import logging
from openai import (
    RateLimitError,
    AuthenticationError,
    APIConnectionError,
    APITimeoutError,
    InternalServerError,
    APIStatusError,
    APIError
    )
from exceptions.AIServiceException import (
            AIRateLimitException,
            AIAuthenticationException,
            AIConnectionException,
            AITimeoutException,
            AIServiceException,
            AIServiceUnavailableException
        )

'''
Service for handling retries with exponential backoff for OpenAI API calls.
'''
class OpenAIRetryService:

    def __init__(self):
        self.logger = logging.getLogger("uvicorn.error")

    """
    Runs an asynchronous OpenAI operation with retries for temporary failures and maps
    the final error to an AIServiceException subclass with the appropriate response code.

    Error handling:
    1. RateLimitError (OpenAI HTTP 429) means the upstream rate or quota limit was reached.
        It is retried; after the last attempt, AIRateLimitException returns HTTP 429 
        (Too Many Requests) to the caller.
    2. APITimeoutError means OpenAI did not respond before the request timed out. It is
        retried; after the last attempt, AITimeoutException returns HTTP 504
        (Gateway Timeout) to indicate the upstream response was not received in time.
    3. APIConnectionError means the service could not establish or maintain a connection
        to OpenAI. It is retried; after the last attempt, AIConnectionException returns
        HTTP 503 (Service Unavailable).
    4. AuthenticationError (OpenAI HTTP 401) means the upstream credentials were rejected.
        It is not retried; AIAuthenticationException returns HTTP 401 (Unauthorized).
    5. InternalServerError (OpenAI HTTP 5xx) indicates an upstream OpenAI server failure.
        It is retried; after the last attempt, AIServiceUnavailableException returns
        HTTP 503 (Service Unavailable).
    6. APIStatusError with HTTP 408 (Request Timeout) or 409 (Conflict) is treated as
        transient and retried; after the last attempt it returns HTTP 503 through
        AIServiceUnavailableException. Other HTTP status errors are not retried and map
        to AIServiceException's default HTTP 500 (Internal Server Error).
    7. Other APIError instances are not retried and map to AIServiceException's default
        HTTP 500 (Internal Server Error). Unexpected non-API exceptions are logged and
        re-raised for the application's general error handling.

    The service makes up to three attempts and waits 1 second, then 2 seconds, between
    retryable attempts.

    Args:
        operation: An asynchronous callable that performs the OpenAI request.

    Returns:
        The result returned by the operation.
            """
    async def execute(self, operation):

        #Set up maximum attempts to retryable routine
        max_attempts = 3
        
        #Set up seconds to sleep in case of retry routine
        delay = 1

        for attempt in range(1, max_attempts + 1):
                   
            try:

                result = await operation()
                return result

            # Exceptions in case of error
            except RateLimitError as e:
                self.logger.warning("Too many messages sent")
                if attempt == max_attempts:
                    raise AIRateLimitException(
                        "AI service rate limit exceeded"
                    ) from e

            except APITimeoutError as e:
                self.logger.warning("Too long for the API to give an answer")
                if attempt == max_attempts:
                    raise AITimeoutException(
                        "AI service request timed out"
                    ) from e

            except APIConnectionError as e:
                self.logger.warning("Could not connect to the API")
                if attempt == max_attempts:
                    raise AIConnectionException(
                        "Could not connect to AI service"
                    ) from e
                
            except AuthenticationError as e:
                self.logger.exception("Could not authenticate the user")
                raise AIAuthenticationException(
                    "AI service authentication failed"
                ) from e
            except InternalServerError as e:
                self.logger.warning(f"OpenAI server error (attempt {attempt}/{max_attempts})")
                if attempt == max_attempts:
                    raise AIServiceUnavailableException(
                        "OpenAI service temporarily unavailable"
                    ) from e

            except APIStatusError as e:
                if e.status_code in (408, 409):
                    self.logger.warning(f"Transient HTTP error {e.status_code} " f"(attempt {attempt}/{max_attempts})")
                    if attempt == max_attempts:
                        raise AIServiceUnavailableException(
                            "OpenAI service temporarily unavailable"
                        ) from e
                else:
                    self.logger.exception(f"Unexpected OpenAI HTTP error: {e.status_code}")
                    raise AIServiceException(
                        "Unexpected AI service HTTP error"
                    ) from e
            
            except APIError as e:
                self.logger.exception("Error with the API")
                raise AIServiceException(
                    "Unexpected AI service error",
                ) from e

            except Exception:
                self.logger.exception("Unexpected error...")
                raise

            await asyncio.sleep(delay)
            delay *= 2
