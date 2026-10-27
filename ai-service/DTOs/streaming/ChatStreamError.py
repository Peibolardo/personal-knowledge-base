from pydantic import BaseModel

'''
Data Transfer Object for Error Event Type for the SSE architecture.
Contains the information from the OpenAI Stream response in case of error.
'''
class ChatStreamError(BaseModel):
    message: str
    code: str
