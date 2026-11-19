from fastapi import APIRouter, Depends
from services.CapabilitiesService import CapabilitiesService
from DTOs.config.ModelCapabilities import ModelCapabilities
from functools import lru_cache
import logging

# Creation of the router
router = APIRouter(
    prefix="/capabilities"  # Predefine the endpoints as /capabilities (Same as RequestMapping in Spring)
)

logger = logging.getLogger("uvicorn.error")


# Dependency injection function (replaces Spring's @Autowired)
# Added @lru_cache to reuse last instance of the service and avoid creating a new one for each request
@lru_cache
def get_capabilities_service() -> CapabilitiesService:
    return CapabilitiesService()


'''
GET /capabilities
Purpose: Retrieve the capabilities of the LLM model
Request Body: None
service: Injected service dependencies to make use of them
'''
@router.get("")
async def get_capabilities(
    service: CapabilitiesService = Depends(get_capabilities_service)
    ) -> ModelCapabilities:
    
    logger.info("Received the request to GET the capabilities of the LLM model")
    
    capabilities = service.get_capabilities()

    logger.info("Successfully retrieved the capabilities of the LLM model")

    return capabilities
