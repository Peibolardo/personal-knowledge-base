from openai import OpenAI
from DTOs.ChatRequest import ChatRequest
from DTOs.ChatResponse import ChatResponse
import logging

class OpenAiService:

    def __init__(self, model: str):
        self.client = OpenAI()
        self.model = model
        self.logger = logging.getLogger("uvicorn.error")

    async def send_message_to_api(request_data: ChatRequest) -> ChatResponse:

        logger.info("Attempting to send a message to the AI")

        response = await self.client.chat.completions.create(
            model= self.model,
            messages=[{"role": "user", "content": request_data.user_message}]
        )

        return ChatResponse(
            response=response.choices[0].message.content,
            tokens_prompt=response.usage.prompt_tokens,
            tokens_completion=response.usage.completion_tokens,
            tokens_total=response.usage.total_tokens,
            model_used=response.model
        )


