from pydantic import BaseModel

'''
Data Transfer Object for Done Event Type for the SSE architecture.
Contains information regarding the token used and marks the end of the Stream response.
'''
class ChatStreamDone(BaseModel):
    tokens_prompt: int
    tokens_completion: int
    tokens_total: int
