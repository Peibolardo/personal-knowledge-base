import os
import time
from openai import (
    OpenAI,
    RateLimitError,
    AuthenticationError,
    APIConnectionError,
    APITimeoutError,
    APIError
)
from DTOs.ChatResponse import ChatResponse
from DTOs.Message import Message
from exceptions.AIServiceException import (
            AIRateLimitException,
            AIAuthenticationException,
            AIConnectionException,
            AITimeoutException,
            AIServiceException
        )
import logging
from typing import List
from services.TokenizerService import TokenizerService

class OpenAiService:

    #Constructor to initialize the class
    def __init__(self):
        self.client = OpenAI()
        self.logger = logging.getLogger("uvicorn.error")
        self.model = os.getenv("OPENAI_MODEL", "gpt-4o-mini")
        self.system_prompt = os.getenv("SYSTEM_PROMPT")

        #TokenizerService with the model established in the env file
        self.tokenizer = TokenizerService(self.model)

    """
    Function that receives a list of Messages with the context and send it to the OpenAI API
    Receives a JSON with the response and different fields
    """
    def send_message_to_api(self, context: List[Message]) -> ChatResponse:

        try:
            self.logger.info("Attempting to send a message to the AI")
            self.logger.info("AI API request started | model=%s | messages=%d", self.model, len(context))

            start_time = time.perf_counter()

            messages = []

            #Check if the system prompt is null
            if not self.system_prompt:
                raise ValueError("SYSTEM_PROMPT environment variable is not configured")

            messages.append({
                "role": "system",
                "content": self.system_prompt
            })

            for msg in context:
                messages.append({
                    "role": msg.role.value,
                    "content": msg.content
                })
            response = self.client.chat.completions.create(
                        model = self.model,
                        messages = messages
                    )

            ## Tokens used by the response message
            tokens_input_message = self.tokenizer.count_tokens(context[-1].content)

            duration_ms = (time.perf_counter() - start_time) * 1000

            self.logger.info(
                "OpenAI request completed | model=%s | messages=%d | "
                "prompt_tokens=%d | completion_tokens=%d | total_tokens=%d | "
                "duration_ms=%d",
                response.model,
                len(context),
                response.usage.prompt_tokens,
                response.usage.completion_tokens,
                response.usage.total_tokens,
                duration_ms
            )

            return ChatResponse(
                content=response.choices[0].message.content,
                tokens_prompt=response.usage.prompt_tokens,
                tokens_completion=response.usage.completion_tokens,
                tokens_total=response.usage.total_tokens,
                tokens_input_message = tokens_input_message,
                model_used=response.model
            )

        except RateLimitError as e:
            self.logger.exception("Too many messages sent")
            raise AIRateLimitException(
                "AI service rate limit exceeded"
            ) from e

        except APIConnectionError as e:
            self.logger.exception("Could not connect to the API")
            raise AIConnectionException(
                "Could not connect to AI service"
            ) from e

        except APITimeoutError as e:
            self.logger.exception("Too long for the API to give an answer")
            raise AITimeoutException(
                "AI service request timed out"
            ) from e

            
        except AuthenticationError as e:
            self.logger.exception("Could not authenticate the user")
            raise AIAuthenticationException(
                "AI service authentication failed"
            ) from e
        
        except APIError as e:
            self.logger.exception("Error with the API")
            raise AIServiceException(
                "Unexpected AI service error"
            ) from e

        except Exception:
            self.logger.exception("Unexpected error...")
            raise

