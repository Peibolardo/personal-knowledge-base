import os
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
from DTOs.streaming import (
    ChatStreamEventType,
    ChatStreamContent,
    ChatStreamMetadata,
    ChatStreamDone,
    ChatStreamError
)

class ChatStreamingService:

    def __init__(self):
        self.client = AsyncOpenAI()
        self.logger = logging.getLogger("uvicorn.error")
        self.model = os.getenv("OPENAI_MODEL", "gpt-4o-mini")
        self.system_prompt = os.getenv("SYSTEM_PROMPT")
        
        #TokenizerService with the model established in the env file
        self.tokenizer = TokenizerService(self.model)

    '''
    Function that receives a list of Messages with the context and send it to the OpenAI API
    Receives from the LLM model chunks with different information for each one
    Sends to the API JSON structured { model: self.model, messages : { role: system, content: self.system_prompt}, { role: user, ...}, {role: assistant. ...}, { role:user, ...}, ... }
    
    Stream mode active
    '''
    async def send_message_to_ai_streaming(self, context: List[Message]) -> AsyncIterator[str]:

        self.logger.info("Attempting to send a message to the AI")
        self.logger.info("AI API request started | model=%s | messages=%d", self.model, len(context))

        messages = []
        
        #Check if the system prompt is null
        if not self.system_prompt:
            raise ValueError("SYSTEM_PROMPT environment variable is not configured")

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

        ## Tokens used by the response message
        tokens_input_message = self.tokenizer.count_tokens(context[-1].content)
        
        stream = await self.client.chat.completions.create(
                    model = self.model,
                    messages = messages,
                    stream = True,
                    stream_options={
                        "include_usage": True
                        },
                )

        metadata = self.buildMetadaStream(tokens_input_message)
        yield metadata

        tokens_prompt = 0
        tokens_completion = 0
        tokens_total = 0

        async for chunk in stream:
            # Inspect usage even if chunk.choices is empty.
            if chunk.usage is not None:
                tokens_prompt = chunk.usage.prompt_tokens
                tokens_completion = chunk.usage.completion_tokens
                tokens_total = chunk.usage.total_tokens

            if not chunk.choices:
                    continue
            
            delta = chunk.choices[0].delta
            content = self.buildContentStream(delta.content)

            yield content



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
    def buildDoneStream(tokens_promptS: int,
                        tokens_completionS: int,
                        tokens_totalS: int)-> ChatStreamMetadata:

        return ChatStreamDone(
            tokens_prompt = tokens_promptS,
            tokens_completion = tokens_completionS,
            tokens_total = tokens_totalS
        )

    '''
    Function to build Error Stream Event
    '''
    def buildErrorStream(messageS: str, codeS : str, retryeableS: bool) -> ChatStreamMetadata:

        return ChatStreamError(
            message = messageS,
            code = codeS,
            retryeable = retryeableS

        )