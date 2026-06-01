from pydantic import BaseModel

"""
Chat Response DTO with the information recieved from the API
Variables
response: Clear text for the backend
tokens_prompt: Number of tokens used for the request
tokens_completion: Number of tokens used for the response
tokens_total: Sum of the other two
model_used:  Represents the name of the AI model to use
"""
class chatRequest(BaseModel):

    response: str
    tokens_prompt: int
    tokens_completion: int
    tokens_total: int
    model_used: str