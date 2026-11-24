from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict

'''
Settings for configuring the model and text chunking parameters used for embeddings.

model -> Selects the embedding model and reads OPENAI_EMBEDDING_MODEL, defaulting to text-embedding-3-small.
chunk_size -> Sets the maximum number of tokens in each chunk and reads EMBEDDING_CHUNK_SIZE, defaulting to 300.
overlap -> Sets how many tokens are repeated between consecutive chunks and reads EMBEDDING_OVERLAP, defaulting to 30.

Settings are loaded from the .env file, and unrecognized configuration values are ignored.
'''
class EmbeddingsConfig(BaseSettings):

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore"
    )

    model : str = Field(
        default="text-embedding-3-small",
        validation_alias="OPENAI_EMBEDDING_MODEL"
    )

    chunk_size: int = Field(
        default=300, 
        validation_alias="EMBEDDING_CHUNK_SIZE"
    )

    overlap: int = Field(
        default=30,
        validation_alias="EMBEDDING_OVERLAP"
    )
