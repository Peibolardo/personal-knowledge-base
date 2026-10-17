package backend.sse.utils;

import backend.sse.model.ChatStreamEventType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

/**
 * Bean Class to serialize objects to String.
 * Focus on the ChatStreamEventType objects from sse DTOs class
 */
@Component
public class ChatStreamEventSerializer {

    private final ObjectMapper objectMapper;

    public ChatStreamEventSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Converts Java Object passed to their JSON representation
     * @param object the object passed to serialize
     * @return String of the JSON serialized object
     */
    public String serialize(Object object){

        try {
           return objectMapper.writeValueAsString(object);
        }
        catch (JsonProcessingException e){
            throw new IllegalStateException("Could not serialize stream event");
        }

    }

}
