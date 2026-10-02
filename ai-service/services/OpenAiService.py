import os
from openai import OpenAI
from DTOs.ChatResponse import ChatResponse
from DTOs.Message import Message
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
    tokenizer = TokenizerService(os.getenv("OPENAI_MODEL", "gpt-4o-mini"))

    """
    Function that receives a list of Messages with the context and send it to the OpenAI API
    Receives a JSON with the response and different fields
    """
    def send_message_to_api(self, context: List[Message]) -> ChatResponse:

        self.logger.info("Attempting to send a message to the AI")

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

        return ChatResponse(
            content=response.choices[0].message.content,
            tokens_prompt=response.usage.prompt_tokens,
            tokens_completion=response.usage.completion_tokens,
            tokens_total=response.usage.total_tokens,
            tokens_input_message = tokens_input_message,
            model_used=response.model
        )


