from pydantic import BaseModel


"""
Query Embedding Response DTO containing the vector for a complete user message.
embedding: Vector generated for the full query without chunking.
embedding_model: Name of the model used to generate the embedding.
"""
class QueryEmbeddingResponse(BaseModel):

    embedding: list[float]
    embedding_model: str
