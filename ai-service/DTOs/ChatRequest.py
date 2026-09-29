from pydantic import BaseModel, Field
from typing import List
from DTOs.Message import Message

"""
Chat Request DTO with the information to send to the API
Variables
model_name: Represents the name of the AI model to use
user_message: Mapped in the JSON as input
"""
class ChatRequest(BaseModel):

    model_name: str = "gpt-4o-mini"
    context: List[Message] = []  # empty on first message