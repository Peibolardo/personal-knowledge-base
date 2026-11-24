from pydantic import BaseModel, ConfigDict, Field

from capabilities.PromptCapabilities import PromptCapabilities

"""
Model capabilities DTO.
Stores the context and completion token limits of the configured AI model, along with prompt token counts.
"""
class ChatModelCapabilities(BaseModel):

    model_config = ConfigDict(populate_by_name=True)

    model: str = Field(
        ...,
        min_length=1,
        description="Name of the AI model used by the service.",
    )

    context_window_tokens: int = Field(
        ...,
        alias="context_window_tokens",
        gt=0,
        description="Maximum number of tokens supported in the model context window.",
    )

    model_max_completion_tokens: int = Field(
        ...,
        alias="model_max_completions_tokens",
        gt=0,
        description="Maximum number of completion tokens supported by the model.",
    )

    prompts: PromptCapabilities = Field(
        ...,
        description="Prompt token counts for the supported AI operations.",
    )
