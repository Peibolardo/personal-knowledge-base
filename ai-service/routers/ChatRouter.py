from dotenv import load_dotenv
load_dotenv()

from fastapi import FastAPI, Depends
from services.OpenAiService import OpenAiService
from DTOs.ChatRequest import ChatRequest
from DTOs.ChatResponse import ChatResponse

import logging

app = FastAPI()
logger = logging.getLogger("uvicorn.error")

# Dependency injection function (replaces Spring's @Autowired)
def get_openai_service() -> OpenAiService:
    return OpenAiService()

"""
POST /chat
Purpose: Send a request to OpenIA service
Request Body:
chatRequest: (Required) chatRequest
service: Injected service dependencies to make use of them
"""
@app.post("/chat", response_model = ChatResponse)
async def send_message_to_api(
    request_data: ChatRequest,
    service: OpenAiService = Depends(get_openai_service)
    ) -> ChatResponse:
    
    logger.info("Received the request to POST a message to the AI")
    
    response = service.send_message_to_api(request_data)

    return response