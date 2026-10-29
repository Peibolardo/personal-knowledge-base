from pydantic import BaseModel
from typing import List
from DTOs.Message import Message

"""
ConversationSummary DTO class
This class is used to represent the current summary of a conversation along with the list of  new messages to add to the summary.
currentSummary: The current summary of the conversation.
messageDTOList: A list of Message DTOs that represent the new messages to be added to the summary
"""
class ConversationSummary(BaseModel):

    currentSummary: str | None = None
    messageDTOList: List[Message] = []  # empty on first message