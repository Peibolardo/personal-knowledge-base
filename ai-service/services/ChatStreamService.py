import os
import asyncio
import time
import logging
from openai import (
    AsyncOpenAI,
    RateLimitError,
    AuthenticationError,
    APIConnectionError,
    APITimeoutError,
    APIError
)
from typing import List
from DTOs.Message import Message
from collections.abc import AsyncIterator
from services.TokenizerService import TokenizerService
from services.SerializeService import SerializeService
from DTOs.streaming.ChatStreamEventType import ChatStreamEventType
from DTOs.streaming.ChatStreamContent import ChatStreamContent
from DTOs.streaming.ChatStreamMetadata import ChatStreamMetadata
from DTOs.streaming.ChatStreamDone import ChatStreamDone
from DTOs.streaming.ChatStreamError import ChatStreamError
from exceptions.AIServiceException import (
            AIRateLimitException,
            AIAuthenticationException,
            AIConnectionException,
            AITimeoutException,
            AIServiceException
        )

class ChatStreamingService:

    def __init__(self):
        self.client = AsyncOpenAI(max_retries=0)
        self.logger = logging.getLogger("uvicorn.error")
        self.model = os.getenv("OPENAI_MODEL", "gpt-4o-mini")
        self.system_prompt = os.getenv("SYSTEM_PROMPT")
        
        #TokenizerService with the model established in the env file
        self.tokenizer = TokenizerService(self.model)

        #SerializerrService
        self.serializer = SerializeService()

    """
    Function receives a list of Messages with the context and send it to the OpenAI API
    Sends to the API JSON structured { model: self.model, messages : { role: system, content: self.system_prompt}, { role: user, ...}, {role: assistant. ...}, { role:user, ...}, ... }

    Receives response from the LLM model, if 200 then opens streaming
    Throws exception in case of any error when requesting to the LLM model API 
    """
    async def prepare_stream(self, context: List[Message]):

        self.logger.info("Attempting to send a message to the AI")
        self.logger.info("AI API request started | model=%s | messages=%d", self.model, len(context))

        start_time = time.perf_counter()

        messages = []
        
        #Check if the system prompt is null
        if not self.system_prompt:
            raise ValueError("SYSTEM_PROMPT environment variable is not configured")
        
        #Check if there is context
        if not context:
                    raise ValueError("The conversation context cannot be empty")

        # ADD system prompt to the call
        messages.append({
            "role": "system",
            "content": self.system_prompt
        })

        # Build the context to send
        for message in context:
            messages.append({
                "role": message.role,
                "content": message.content
            })

        ## Tokens used by the request message
        tokens_input_message = self.tokenizer.count_tokens(context[-1].content)

        # Stream opening request
        stream = await self.open_stream_with_retries(messages)

        return stream, tokens_input_message, start_time
        

    '''
    Function that receives the opened stream and sends the information to the client in a streaming way
    Receives from the LLM model chunks with different information through the stream
    
    Stream mode active
    Builds DTOs of the different events depending on the information contained.
    Return serialized DTOs using the SerializerService based on the ChatStreamEventType Enum
    '''
    async def send_message_to_ai_streaming(self, stream,tokens_input_message: int, start_time: float, context_length: int) -> AsyncIterator[str]:

        try:

            #Builds metdata stream event and sends it to the client
            metadata = self.build_metadata_stream(tokens_input_message)
            yield self.serializer.serialize_event(metadata, ChatStreamEventType.METADATA)
    
            #Variable to check if the usage has been received in the stream
            usage_received = False

            #Iterate all chunks from the stream response
            async for chunk in stream:
        
                if chunk.choices:
                        delta = chunk.choices[0].delta

                        if delta.content:
                        
                            content = self.build_content_stream(delta.content)
                            yield self.serializer.serialize_event(content, ChatStreamEventType.CONTENT)

                # Inspect usage of the request and save it.
                if chunk.usage is not None:
                    tokens_prompt = chunk.usage.prompt_tokens
                    tokens_completion = chunk.usage.completion_tokens
                    tokens_total = chunk.usage.total_tokens

                    usage_received = True

            # The stream ended. Validate usage before declaring success.
            if not usage_received:

                self.logger.error(
                    "AI stream ended without token usage information"
                )

                error = self.build_error_stream(
                    message="AI response ended without usage information",
                    code="USAGE_UNAVAILABLE"
                )

                yield self.serializer.serialize_event(
                    error,
                    ChatStreamEventType.ERROR
                )

                return

            duration_ms = (time.perf_counter() - start_time) * 1000

            self.logger.info(
                "OpenAI streaming request completed | model=%s | messages=%d | "
                "prompt_tokens=%d | completion_tokens=%d | total_tokens=%d | "
                "duration_ms=%.0f",
                self.model,
                context_length,
                tokens_prompt,
                tokens_completion,
                tokens_total,
                duration_ms
            )

            # If stream completes correctly then build Done event and send it to finish the stream
            done = self.build_done_stream(tokens_prompt, tokens_completion, tokens_total)
            yield self.serializer.serialize_event(done, ChatStreamEventType.DONE)

            return

        except APITimeoutError as e:

            self.logger.exception("Error %s: AI stream timed out", e)
            
            error = self.build_error_stream(
                message = "AI service request timed out",
                code = "UPSTREAM_TIMEOUT_ERROR"
                )

            yield self.serializer.serialize_event(
                error,
                ChatStreamEventType.ERROR
            )

            return

        except APIConnectionError as e:

            self.logger.exception("Error %s: cannot connect to AI stream", e)

            error = self.build_error_stream(
                message= "Could not connect to the AI service.",
                code= "UPSTREAM_CONNECTION_ERROR"
                )
            
            yield self.serializer.serialize_event(
                error,
                ChatStreamEventType.ERROR
            )

            return

        except APIError as e:

            self.logger.exception("Error %s: Unexpected error", e)
            
            error = self.build_error_stream(
                message = "Unexpected AI service error",
                code = "UPSTREAM_ERROR"
            )
            yield self.serializer.serialize_event(
                error,
                ChatStreamEventType.ERROR
            )

            return

        except asyncio.CancelledError:
            self.logger.info("AI stream cancelled by the downstream client")
            raise

        except Exception as e:

            self.logger.exception("Error %s: Unexpected error", e)

            error = self.build_error_stream(
                message = "Unexpected error..",
                code = "UPSTREAM_ERROR"
            )
            yield self.serializer.serialize_event(
                error,
                ChatStreamEventType.ERROR
            )

            return
        
        finally:
            await stream.close()


    '''
    Function to build Metadata Stream Event
    '''
    def build_metadata_stream(self, tokens_request: int) -> ChatStreamMetadata:

        return ChatStreamMetadata(
            tokens_input_message=tokens_request,
            model=self.model
        )


    '''
    Function to build Content Stream Event
    '''
    def build_content_stream(self, content: str) -> ChatStreamContent:
        return ChatStreamContent(content=content)


    '''
    Function to build Done Stream Event
    '''
    def build_done_stream(self, tokens_promptS: int,
                        tokens_completionS: int,
                        tokens_totalS: int)-> ChatStreamDone:

        return ChatStreamDone(
            tokens_prompt = tokens_promptS,
            tokens_completion = tokens_completionS,
            tokens_total = tokens_totalS
        )


    '''
    Function to build Error Stream Event
    '''
    def build_error_stream(self, messageS: str, codeS : str) -> ChatStreamError:

        return ChatStreamError(
            message = messageS,
            code = codeS,
        )


    '''
    Function to open stream connection to the LLM Model API
    
    Throw exceptions depending on the error returned by the AI API
    Retryable errors have a maximum of three attemps before stopping the flow
    '''
    async def open_stream_with_retries(self, content):

        #Set up maximum attempts to retryable routine
        max_attempts = 3

        #Set up seconds to sleep in case of retry routine
        delay = 1
        for attempt in range(1, max_attempts + 1):

            # Request to the model API
            try:
                stream = await self.client.chat.completions.create(
                            model = self.model,
                            messages = content,
                            stream = True,
                            stream_options={
                                "include_usage": True
                                },
                        )

                return stream

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
            delay += delay