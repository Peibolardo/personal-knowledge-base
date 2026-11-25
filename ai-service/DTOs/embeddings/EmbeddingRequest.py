from pydantic import BaseModel, Field

"""
Embedding Request DTO with the content to split into chunks and embed.
content: Text whose chunks will be embedded.
"""
class EmbeddingRequest(BaseModel):

    content: str = Field(..., min_length=1)
