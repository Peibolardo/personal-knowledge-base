from openai import OpenAI
from DTOs.ChatRequest import ChatRequest
from DTOs.ChatResponse import ChatResponse
import logging

class OpenAiService:

    def __init__(self):
        self.client = OpenAI()
        self.logger = logging.getLogger("uvicorn.error")

    def send_message_to_api(self, request_data: ChatRequest) -> ChatResponse:

        self.logger.info("Attempting to send a message to the AI")

        response = self.client.chat.completions.create(
            model = request_data.model_name,
            messages = [{"role": "user", "content": request_data.user_message}]
        )

        return ChatResponse(
            response=response.choices[0].message.content,
            tokens_prompt=response.usage.prompt_tokens,
            tokens_completion=response.usage.completion_tokens,
            tokens_total=response.usage.total_tokens,
            model_used=response.model
        )


