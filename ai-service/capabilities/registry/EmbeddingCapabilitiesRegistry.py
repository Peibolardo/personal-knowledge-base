"""
Registry of supported model limits used by the AI service.
Each entry maps a model name to its context window and maximum completion-token limit.
"""
MODEL_CAPABILITIES = {
    "text-embedding-3-small": {
        "dimensions": 1536,
    },
    "text-embedding-3-large": {
        "dimensions": 3072,
    },
}

'''
Registry class used to retrieve known token limits for supported embedding models.
'''
class EmbeddingCapabilitiesRegistry:

    '''
    Retrieves the embedding dimensions for a registered model.

    model: Model name supplied by OpenAIConfig.
    Returns: Registered embedding dimensions.
    Raises: ValueError if the model name is blank or not in the registry.
    '''
    def get_embedding_capabilities(self, model: str) -> dict[str, int]:
        if not model or not model.strip():
            raise ValueError("The configured embedding model name cannot be blank")

        if model not in MODEL_CAPABILITIES:
            raise ValueError(f"No capability limits are registered for embedding model '{model}'")

        return MODEL_CAPABILITIES[model]

    