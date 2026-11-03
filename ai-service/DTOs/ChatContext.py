from pydantic import BaseModel
from typing import List
from DTOs.Message import Message

"""
ChatContext DTO class
This class is used to represent the context of the conversation with current summary of a conversation along with the list of messages.
currentSummary: The current summary of the conversation.
messageDTOList: A list of Message DTOs that represent the new messages to be added to the summary
""" 
class ChatContext(BaseModel):

    currentSummary: str | None = None
    messageDTOList: List[Message] = []  # empty on first message