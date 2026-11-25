from pydantic import BaseModel

"""
Embedding Chunk DTO with the content and embedding metadata for one chunk.
content: Text included in the chunk.
chunk_index: Zero-based position of the chunk in the source content.
token_count: Number of tokens in the chunk.
embedding: Vector generated for the chunk.
embedding_model: Name of the model used to generate the embedding.
"""
class EmbeddingChunk(BaseModel):

    content: str
    chunk_index: int
    token_count: int
    embedding: list[float] 
    embedding_model: str
