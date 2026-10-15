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
     * Utilize JsonNode to extract information in different nodes
     * Compares the "event" node with ChatStreamEventType to choose to what object to deserialize
     *
     * @param json Streaming event in JSON format
     * @return Streaming event Object
     * @throws IllegalArgumentException in case there is no "event" or "data"
     * @throws IllegalStateException if the deserialize process could not be performed
     */
    public Object deserialize(String json) {

        try {
            JsonNode rootNode = objectMapper.readTree(json);

            JsonNode eventNode = rootNode.get("event");
            JsonNode dataNode = rootNode.get("data");

            if (eventNode == null || !eventNode.isTextual()){
                throw new IllegalArgumentException("There is no event in the JSON to deserialize");
            }

            if (dataNode == null || dataNode.isNull()){
                throw new IllegalArgumentException("There is no data in the JSON to deserialize");
            }

            ChatStreamEventType eventType = ChatStreamEventType.valueOf(eventNode.asText().toUpperCase(Locale.ROOT));

            return switch (eventType) {
                case METADATA -> objectMapper.treeToValue(dataNode,
                        ChatStreamMetadataDTO.class
                );
                case DONE -> objectMapper.treeToValue(dataNode,
                        ChatStreamDoneDTO.class
                );
                case ERROR -> objectMapper.treeToValue(dataNode,
                        ChatStreamErrorDTO.class
                );
                case CONTENT -> objectMapper.treeToValue(dataNode,
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
