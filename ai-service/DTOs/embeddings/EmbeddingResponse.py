from pydantic import BaseModel
from typing import List
from DTOs.EmbeddingChunk import EmbeddingChunk

"""
Embedding Response DTO containing the chunks generated from the source content.
chunks: List of content chunks and their embeddings.
"""
class EmbeddingResponse(BaseModel):

    chunks: List[EmbeddingChunk]
