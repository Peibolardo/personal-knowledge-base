from pydantic import BaseModel
from DTOs.streaming import ChatStreamEventType

'''
Service with the objective to serialize the information of each event to send it through AsyncInterator[str]
event -> represents our DTO passed depending on the event(Metadata, Error, Content, Done)
event_type -> The enum created to restrict our Stream Events
'''
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

