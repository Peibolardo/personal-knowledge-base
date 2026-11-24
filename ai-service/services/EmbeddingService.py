import os
import logging
from openai import (
    AsyncOpenAI,
)
from infraestructure.TokenizerService import TokenizerService
from infraestructure.OpenAIRetryService import OpenAIRetryService
from DTOs.EmbeddingRequest import EmbeddingRequest
from DTOs.EmbeddingResponse import EmbeddingResponse
from DTOs.QueryEmbeddingResponse import QueryEmbeddingResponse
from config.EmbeddingsConfig import EmbeddingsConfig
from capabilities.registry.EmbeddingCapabilitiesRegistry import EmbeddingCapabilitiesRegistry
from exceptions.AIServiceException import AIServiceException


class EmbeddingService:
    
    def __init__(self):
        self.client = AsyncOpenAI(max_retries=0)
        self.logger = logging.getLogger("uvicorn.error")

        #TokenizerService with the model established in the env file
        self.tokenizer = TokenizerService(self.model)

        #Service to exeecute the OpenAI API calls with retry logic
        self.retry_service = OpenAIRetryService()

        #EmbeddingsConfig to retrieve the model, chunk size, and overlap values from the environment
        self.embeddings_config = EmbeddingsConfig()

        #EmbeddingCapabilitiesRegistry to retrieve the embedding model's capabilities, including dimensions
        self.registry = EmbeddingCapabilitiesRegistry()

        try:
            self.model_limits = self.registry.get_embedding_capabilities(self.model)
        except ValueError as error:
            raise AIServiceException(str(error)) from error

        # Set the embedding model from the embeddings configuration, raising an exception if it cannot be retrieved
        try:    
            self.model = self.embeddings_config.model
        except ValueError as error:
            raise AIServiceException("Cannot retrieve the embedding model from the environment configuration") from error

        # Set the chunk size and overlap values from the embeddings configuration
        self.chunk_size = self.embeddings_config.chunk_size
        self.overlap = self.embeddings_config.overlap

        # Set the embedding dimensions from the model limits retrieved from the registry
        self.dimensions = self.model_limits["dimensions"]


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

        embedding_operation = lambda: self.client.embeddings.create(
            model=self.model,
            input=chunk_contents
        )

        response = await self.retry_service.execute(embedding_operation)

        embeddings_by_index = sorted(response.data, key=lambda item: item.index)

        # Check that the embeddings returned by the API match the expected dimensions and order 
        for embedding_data in embeddings_by_index:
            if len(embedding_data.embedding) != self.dimensions:
                raise ValueError("Unexpected embedding dimensions")

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


    '''
    Function to generate one embedding for the complete user query without splitting it into chunks.

    query: The complete user message to embed for semantic comparison against stored message chunks.
    The text is sent directly to the embeddings API, and the response includes the vector and model name.
    '''
    async def embed_query(self, query: str) -> QueryEmbeddingResponse:

        if not query or not query.strip():
            raise ValueError("Query content cannot be blank")

        self.logger.info("Generating an embedding for the complete user query")

        query_embedding_operation = lambda: self.client.embeddings.create(
            model=self.model,
            input=query
        )

        response = await self.retry_service.execute(query_embedding_operation)

        if len(response.data) != 1 or response.data[0].index != 0:
            self.logger.error("Embedding API returned an unexpected result for the query")
            raise ValueError("Embedding API did not return exactly one query embedding")

        query_embedding = QueryEmbeddingResponse(
            embedding=response.data[0].embedding,
            embedding_model=self.model
        )

        self.logger.info("Successfully generated the query embedding")
        return query_embedding
