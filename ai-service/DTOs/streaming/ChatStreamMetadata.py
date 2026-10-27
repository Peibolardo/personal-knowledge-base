from pydantic import BaseModel

'''
Data Transfer Object for Metadata Event Type for the SSE architecture.
Contains the basic information from the OpenAI Stream response.
'''
class ChatStreamMetadata(BaseModel):
    tokens_input_message: int
    model: str
