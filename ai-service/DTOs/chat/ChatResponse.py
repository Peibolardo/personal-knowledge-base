from pydantic import BaseModel

"""
Chat Response DTO with the information received from the API
Variables
content: Clear text for the backend
tokens_prompt: Number of tokens used for the request
tokens_completion: Number of tokens used for the response
tokens_total: Sum of the other two
tokens_input_message: Number of tokens belonging to the last message
model_used:  Represents the name of the AI model to use
"""
class ChatResponse(BaseModel):

    content: str
    tokens_prompt: int
    tokens_completion: int
    tokens_total: int
    tokens_input_message: int
    model_used: str