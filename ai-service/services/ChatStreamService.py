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
        
        response = await self.client.chat.completions.create(
                    model = self.model,
                    messages = messages,
                    stream = True,
                    stream_options={
                        "include_usage": True
                        },
                )