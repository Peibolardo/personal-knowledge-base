from pydantic import BaseModel

"""
Chat Request DTO with the information to send to the API
Variables
model_name: Represents the name of the AI model to use
user_message: Mapped in the JSON as input
"""
class chatRequest(BaseModel):

    model_name: str = "gpt-4o-mini"
    user_message: str = Field(..., alias="input", min_length=1)