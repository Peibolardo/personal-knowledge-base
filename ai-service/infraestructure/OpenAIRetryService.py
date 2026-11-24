import asyncio
import logging
from openai import (
    RateLimitError,
    AuthenticationError,
    APIConnectionError,
    APITimeoutError,
    APIError
    )
from exceptions.AIServiceException import (
            AIRateLimitException,
            AIAuthenticationException,
            AIConnectionException,
            AITimeoutException,
            AIServiceException
        )

'''
Service for handling retries with exponential backoff for OpenAI API calls.
'''
class OpenAIRetryService:

    def __init__(self):
        self.logger = logging.getLogger("uvicorn.error")

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