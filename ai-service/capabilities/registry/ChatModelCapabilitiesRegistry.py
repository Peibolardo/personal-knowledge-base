"""
Registry of supported model limits used by the AI service.
Each entry maps a model name to its context window and maximum completion-token limit.
"""
MODEL_CAPABILITIES = {
    "gpt-4o-mini": {
        "context_window_tokens": 128000,
        "max_completion_tokens": 16384
    },
    "gpt-5-mini": {
        "context_window_tokens": 400000,
        "max_completion_tokens": 128000
    }
}


'''
Registry class used to retrieve known token limits for supported models.
'''
class ChatModelCapabilitiesRegistry:

    '''
    Retrieves the context window and maximum completion-token limits for a registered model.

    model: Model name supplied by OpenAIConfig.
    Returns: Registered context-window and maximum-completion token counts.
    Raises: ValueError if the model name is blank or not in the registry.
    '''
    def get_model_capabilities(self, model: str) -> dict[str, int]:
        if not model or not model.strip():
            raise ValueError("The configured model name cannot be blank")

        if model not in MODEL_CAPABILITIES:
            raise ValueError(f"No capability limits are registered for model '{model}'")

        return MODEL_CAPABILITIES[model]

    '''
    Retrieves the registered context-window token limit for the specified model.

    model: Model name supplied by OpenAIConfig.
    Returns: Maximum number of tokens supported in the model context window.
    '''
    def get_context_window_tokens(self, model: str) -> int:
        return self.get_model_capabilities(model)["context_window_tokens"]

    '''
    Retrieves the registered maximum completion-token limit for the specified model.
    
    model: Model name supplied by OpenAIConfig.
    Returns: Maximum number of tokens the model can generate in one completion.
    '''
    def get_max_completion_tokens(self, model: str) -> int:
        return self.get_model_capabilities(model)["max_completion_tokens"]
