from fastapi import APIRouter, Depends
from services.CapabilitiesService import CapabilitiesService
from capabilities.ModelCapabilities import ModelCapabilities
from functools import lru_cache
import logging

# Create the router for the AI service model-capability endpoint.
router = APIRouter(
    prefix="/capabilities",
)

logger = logging.getLogger("uvicorn.error")


'''
Function to provide the CapabilitiesService dependency.
The service instance is cached so its configuration, prompt files, and calculated token counts are reused across requests.
'''
@lru_cache
def get_capabilities_service() -> CapabilitiesService:
    return CapabilitiesService()


'''
GET /capabilities
Purpose: Return the configured model limits and static prompt token counts for chat and summarization.
service: Cached CapabilitiesService dependency used to retrieve the precomputed capability DTO.
Returns: ModelCapabilities serialized with the JSON field names expected by the Spring backend.
'''
@router.get("/chat", response_model=ModelCapabilities, response_model_by_alias=True)
async def get_capabilities(
    service: CapabilitiesService = Depends(get_capabilities_service)
) -> ModelCapabilities:
    logger.info("Received a request for AI model capabilities")

    capabilities = service.get_capabilities()

    logger.info("Successfully returned AI model capabilities for model %s", capabilities.model)

    return capabilities


@router.get("/embeddings", response_model=ModelCapabilities, response_model_by_alias=True)
