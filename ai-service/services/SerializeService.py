from pydantic import BaseModel
from DTOs.streaming import ChatStreamEventType

class SerializeService:

    def serialize_event(
    self,
    event: BaseModel,
    event_type: ChatStreamEventType
    ) -> str:

        return (
            f"event: {event_type.value}\n"
            f"data: {event.model_dump_json()}\n\n"
        )

