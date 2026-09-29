from pydantic import BaseModel, Field

"""
Message DTO with the information to send to the API
Variables
role: Indicates if the message comes from the user or assistant
content: Content of the message
"""
class Message(BaseModel):

    role: str = Field(..., min_length=1)
    content: str = Field(..., min_length=1)