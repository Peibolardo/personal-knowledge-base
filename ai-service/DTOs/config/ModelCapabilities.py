from pydantic import BaseModel, ConfigDict, Field

from DTOs.config.PromptCapabilities import PromptCapabilities

"""
Model capabilities DTO.
Stores the context and completion token limits of the configured AI model, along with prompt token counts.
"""
class ModelCapabilities(BaseModel):

    model: str = Field(
        ...,
        description="Name of the AI model used by the service.",
    )

    context_window_tokens: int = Field(
        ...,
        description="Maximum number of tokens supported in the model context window.",
    )

    model_max_completion_tokens: int = Field(
        ...,
        description="Maximum number of completion tokens supported by the model.",
    )

    prompts: PromptCapabilities = Field(
        ...,
        description="Prompt token counts for the supported AI operations.",
    )
