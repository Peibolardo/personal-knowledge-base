import os
import logging
from openai import (
    AsyncOpenAI,
)
from services.TokenizerService import TokenizerService
from DTOs.EmbeddingRequest import EmbeddingRequest
from DTOs.EmbeddingResponse import EmbeddingResponse


class EmbeddingService:
    
    def __init__(self):
        self.client = AsyncOpenAI()
        self.logger = logging.getLogger("uvicorn.error")
        self.model = os.getenv("OPENAI_EMBEDDING_MODEL", "text-embedding-3-small")

        #TokenizerService with the model established in the env file
        self.tokenizer = TokenizerService(self.model)

        self.chunk_size = 300
        self.overlap = 30


    '''
    Function to generate an embedding for each chunk of the request content.

    request: Embedding request containing the source content.
    The tokenizer returns dictionaries with content, chunk_index, and token_count.
    Their content values are sent together in one embeddings API request.

    The API includes an index on each returned embedding. Sorting by this index restores the input order, then zip pairs each chunk with its corresponding
    embedding. The indexes are checked first because zip would otherwise silently stop when either list is shorter.
    '''
    async def generate_embeddings(self, request: EmbeddingRequest) -> EmbeddingResponse:

        self.logger.info("Generating embeddings for the provided text")

        tokenized_chunks = self.tokenizer.tokenizeAndChunkingText(request.content,chunk_size=self.chunk_size,   overlap=self.overlap)

        if not tokenized_chunks:
            self.logger.warning("No chunks were generated from the provided text")
            raise ValueError("No chunks were generated from the provided text")

        for chunk in tokenized_chunks:
            self.logger.info(f"Chunk {chunk['chunk_index']}: "f"Token count: {chunk['token_count']}")

        chunk_contents = [chunk["content"] for chunk in tokenized_chunks]

        response = await self.client.embeddings.create(
            model=self.model,
            input=chunk_contents
        )

        embeddings_by_index = sorted(response.data, key=lambda item: item.index)

        expected_indices = list(range(len(tokenized_chunks)))
        actual_indices = [embedding_data.index for embedding_data in embeddings_by_index]

        if actual_indices != expected_indices:
            self.logger.error("Embedding response indexes do not match the submitted chunks")
            raise ValueError("Embedding response did not contain one result per input chunk")

        result_chunks = []

        for chunk, embedding_data in zip(tokenized_chunks, embeddings_by_index, strict=True):
            result_chunks.append({
                "content": chunk["content"],
                "chunk_index": chunk["chunk_index"],
                "token_count": chunk["token_count"],
                "embedding": embedding_data.embedding,
                "embedding_model": self.model
            })

        self.logger.info("Successfully generated embeddings for the provided text")

        return EmbeddingResponse(chunks=result_chunks)
