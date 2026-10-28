from fastapi import APIRouter, Depends
from fastapi.responses import StreamingResponse
from services.OpenAiService import OpenAiService
from services.ChatStreamService import ChatStreamingService
from DTOs.Message import Message
from DTOs.ChatResponse import ChatResponse
from typing import List
from functools import lru_cache
from DTOs.ConversationSummary import ConversationSummary

import logging

# Creation of the router
router = APIRouter(
    prefix="/chat"  # Predefine the endpoints as /chat (Same as RequestMapping in Spring)
)

logger = logging.getLogger("uvicorn.error")

# Dependency injection function (replaces Spring's @Autowired)
# Added @lru_cache to reuse last instance of the service and avoid creating a new one for each request
@lru_cache
def get_openai_service() -> OpenAiService:
    return OpenAiService()

# Dependency injection function (replaces Spring's @Autowired)
# Added @lru_cache to reuse last instance of the service and avoid creating a new one for each request
@lru_cache
def get_chat_streaming_service() -> ChatStreamingService:
    return ChatStreamingService()

"""
POST /chat
Purpose: Send a request to OpenIA service
Request Body:
context: (Required) List[Message]
service: Injected service dependencies to make use of them
"""
@router.post("", response_model = ChatResponse)
async def send_message_to_api(
    context: List[Message],
    service: OpenAiService = Depends(get_openai_service)
    ) -> ChatResponse:
    
    logger.info("Received the request to POST a message to the AI")
    
    response = service.send_message_to_api(context)

    logger.info("Successfully received the response from the AI API")

    return response


"""
POST /chat/stream
Purpose: Send a request to OpenIA service and receive the information as chunks via Stream
Request Body:
context: (Required) List[Message]
service: Injected service dependencies to make use of them

Returns streamed events
"""
@router.post("/stream")
async def send_message_to_api_stream(
    context: List[Message],
    service: ChatStreamingService = Depends(get_chat_streaming_service)
    ) -> StreamingResponse:

    logger.info("Received POST request with a message to send it to the LLM model")

    stream, tokens_input_message, start_time = (
        await service.prepare_stream(context)
    )

    logger.info("Successfully requested to the LLM and opened the stream")


    return  StreamingResponse(
        service.send_message_to_ai_streaming(stream, tokens_input_message, start_time, len(context)),
        media_type= "text/event-stream"
        )

"""
POST /chat/summarize
Purpose: Send a request to OpenIA service with the current summary of the conversation and the new messages to summarize and receive the response.
Request Body:
conversationSummary: (Required) ConversationSummary
service: Injected service dependencies to make use of them

Returns ChatResponse with the summarized conversation
"""
@router.post("/summarize")
async def summarize_conversation(
    conversationSummary: ConversationSummary,
    service: OpenAiService = Depends(get_openai_service)
    ) -> str:
    
    logger.info("Received the request to summarize the conversation")

    response = service.summarize_conversation(conversationSummary)

    logger.info("Successfully received the summarized response from the AI API")

    return response
