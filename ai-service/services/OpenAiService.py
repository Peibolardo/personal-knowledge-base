import os
from openai import OpenAI
from DTOs.ChatRequest import ChatRequest
from DTOs.ChatResponse import ChatResponse
from DTOs.Message import Message
import logging
from typing import List

class OpenAiService:

    #Constructor to initialize the class
    def __init__(self):
        self.client = OpenAI()
        self.logger = logging.getLogger("uvicorn.error")
        self.model = os.getenv("OPENAI_MODEL", "gpt-4o-mini")

    """
    Function that receives a list of Messages with the context and send it to the OpenAI API
    Receives a JSON with the response and different fields
    """
    def send_message_to_api(self, context: List[Message]) -> ChatResponse:

        self.logger.info("Attempting to send a message to the AI")

        messages = []
        for msg in context:
            messages.append({
                "role": msg.role,
                "content": msg.content
            })
        response = self.client.chat.completions.create(
                    model = self.model,
                    messages = messages
                )

        return ChatResponse(
            content=response.choices[0].message.content,
            tokens_prompt=response.usage.prompt_tokens,
            tokens_completion=response.usage.completion_tokens,
            tokens_total=response.usage.total_tokens,
            model_used=response.model
        )


