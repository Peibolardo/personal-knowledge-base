from enum import Enum

class ChatStreamEventType(Enum):
    METADATA = "metadata"
    CONTENT = "content"
    DONE = "done"
    ERROR = "error"