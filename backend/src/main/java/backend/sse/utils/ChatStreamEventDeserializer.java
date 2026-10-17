package backend.sse.utils;

import backend.sse.DTOs.ChatStreamContentDTO;
import backend.sse.DTOs.ChatStreamDoneDTO;
import backend.sse.DTOs.ChatStreamErrorDTO;
import backend.sse.DTOs.ChatStreamMetadataDTO;
import backend.sse.model.ChatStreamEventType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Bean Class to deserialize JSON events to ChatStreamEventType objects
 */
@Component
public class ChatStreamEventDeserializer {

    private final ObjectMapper objectMapper;

    public ChatStreamEventDeserializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Deserialize method to convert Streaming JSON events to Stream event DTOs
     *
     * @param eventName Is the event to deserialize
     * @param data Event information
     * @return Streaming event Object
     * @throws IllegalArgumentException in case there is no "event" or "data"
     * @throws IllegalStateException if the deserialize process could not be performed
     */
    public Object deserialize(String eventName, String data) {

        if (eventName == null || data == null) {
            throw new IllegalArgumentException("Stream event without name or data");
        }

        try {

            ChatStreamEventType eventType = ChatStreamEventType.valueOf(eventName.toUpperCase(Locale.ROOT));

            return switch (eventType) {
                case METADATA -> objectMapper.readValue(data,
                        ChatStreamMetadataDTO.class
                );
                case DONE -> objectMapper.readValue(data,
                        ChatStreamDoneDTO.class
                );
                case ERROR -> objectMapper.readValue(data,
                        ChatStreamErrorDTO.class
                );
                case CONTENT -> objectMapper.readValue(data,
                        ChatStreamContentDTO.class
                );
                default -> throw new IllegalArgumentException(
                        "Unsupported event type: " + eventType
                );
            };

        }
        catch(JsonProcessingException e){
            throw new IllegalStateException("Could not deserialize the event passed");
        }

    }

}
