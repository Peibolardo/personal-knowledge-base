from fastapi import APIRouter, Depends
from services.EmbeddingService import EmbeddingService
from functools import lru_cache
from DTOs.EmbeddingRequest import EmbeddingRequest
from DTOs.EmbeddingResponse import EmbeddingResponse

import logging

# Creation of the embedding router
router = APIRouter(
    prefix="/embeddings"  # Predefine the endpoints as /embeddings (Same as RequestMapping in Spring)
)

logger = logging.getLogger("uvicorn.error")


# Dependency injection function (replaces Spring's @Autowired)
# Added @lru_cache to reuse last instance of the service and avoid creating a new one for each request
@lru_cache
def get_embedding_service() -> EmbeddingService:
    return EmbeddingService()


'''
POST /embeddings/generate
Purpose: Generate embeddings for the provided text
Request Body:
req: (Required) The request DTO with the message content for which embeddings need to be generated
service: Injected service dependencies to make use of them
'''
@router.post("/generate")
async def generate_embeddings(
    req: EmbeddingRequest,
    service: "EmbeddingService" = Depends(get_embedding_service)
) -> EmbeddingResponse:
    logger.info("Received the request to generate embeddings for the provided text")
    
    embeddings = await service.generate_embeddings(req)

    logger.info("Successfully generated embeddings for the provided text")

    return embeddings