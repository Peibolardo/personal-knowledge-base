from pydantic import BaseModel

'''
Data Transfer Object for Content Event Type for the SSE architecture.
Contains the content information from each stream chunk.
'''
class ChatStreamContent (BaseModel):
    content : str