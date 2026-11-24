from openai import BaseModel
from pydantic import ConfigDict, Field

"""
Embeddings capabilities DTO.
Stores the model name and embedding dimensions.
"""
class EmbeddingsCapabilities(BaseModel):
     
    model_config = ConfigDict(populate_by_name=True)
     
    model: str = Field(
        ...,
        min_length=1,
        description="Name of the AI embedding model used by the service.",
    )

    dimensions: int = Field(
        ...,
        alias="dimensions",
        gt=0,
        description="The number of dimensions in the embedding space.",
    )