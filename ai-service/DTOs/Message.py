from pydantic import BaseModel, Field
from enum import Enum

"""
Enum to restrict role to MessageRole.
SYSTEM: Represents the System Prompt Message
USER: Represents messages written by the user
ASSISTANT: Represents messages responded by the assistant
"""
class MessageRole(str, Enum):
    SYSTEM = "system"
    USER = "user"
    ASSISTANT = "assistant"


"""
Message DTO with the information to send to the API
Variables
role: Indicates if the message comes from the user or assistant
content: Content of the message
"""
class Message(BaseModel):

    role: MessageRole
    content: str = Field(..., min_length=1)