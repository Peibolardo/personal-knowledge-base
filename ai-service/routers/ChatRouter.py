from fastapi import APIRouter, Depends
from services.OpenAiService import OpenAiService
from DTOs.Message import Message
from DTOs.ChatResponse import ChatResponse
from typing import List

import logging

# Creation of the router
router = APIRouter(
    prefix="/chat"  # Predefine the endoints as /chat (Same as RequestMapping in Spring)
)

logger = logging.getLogger("uvicorn.error")

# Dependency injection function (replaces Spring's @Autowired)
def get_openai_service() -> OpenAiService:
    return OpenAiService()

"""
POST /chat
Purpose: Send a request to OpenIA service
Request Body:
context: (Required) List[Message]
service: Injected service dependencies to make use of them
"""
@router.post("/chat", response_model = ChatResponse)
async def send_message_to_api(
    context: List[Message],
    service: OpenAiService = Depends(get_openai_service)
    ) -> ChatResponse:
    
    logger.info("Received the request to POST a message to the AI")
    
    response = service.send_message_to_api(context)

    return response