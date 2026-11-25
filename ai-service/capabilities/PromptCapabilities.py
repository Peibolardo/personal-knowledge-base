from pydantic import BaseModel, Field

from capabilities.OperationPromptCapabilities import OperationPromptCapabilities

"""
Prompt capabilities DTO.
Groups prompt token usage by AI operation, such as chat and summarization.
"""
class PromptCapabilities(BaseModel):

    chat: OperationPromptCapabilities = Field(
        ...,
        description="Prompt token counts used for chat completions.",
    )

    summarization: OperationPromptCapabilities = Field(
        ...,
        description="Prompt token counts used for conversation summarization.",
    )
