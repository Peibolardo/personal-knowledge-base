from pydantic import BaseModel, ConfigDict, Field

"""
Operation prompt capabilities DTO.
Stores the token counts for the system and developer prompts used by one AI operation.
"""
class OperationPromptCapabilities(BaseModel):

    system_prompt_tokens: int = Field(
        ...,
        description="Number of tokens used by the system prompt.",
    )

    developer_prompt_tokens: int = Field(
        ...,
        description="Number of tokens used by the developer prompt.",
    )
