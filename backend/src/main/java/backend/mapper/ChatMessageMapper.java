package backend.mapper;
import backend.dto.ChatRequestDTO;
import backend.dto.ChatResponseDTO;
import backend.model.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * MapStruct mapper interface for converting between Message entity and ChatRequestDTO or ChatResponseDTO DTOs.
 * The 'componentModel = "spring"' makes it a Spring component, allowing for @Autowired injection.
 */
@Mapper(componentModel = "spring")
public interface ChatMessageMapper {

    ChatMessageMapper INSTANCE = Mappers.getMapper(ChatMessageMapper.class);

    /**
     * Maps a Message entity to a ChatRequestDTO DTO.
     *
     * @param message The Message entity to map.
     * @return The mapped ChatRequestDTO DTO.
     */
    @Mapping(source = "conversation.id", target = "conversationId")
    @Mapping(source = "content", target = "input")
    ChatRequestDTO toDtoRequest(Message message);

    /**
     * Maps a Message entity to a ChatResponseDTO DTO.
     *
     * @param message The Message entity to map.
     * @return The mapped ChatResponseDTO DTO.
     */
    @Mapping(source = "conversation.id", target = "conversationId")
    @Mapping(source = "content", target = "response")
    ChatResponseDTO toDtoResponse(Message message);

    /**
     * Maps a ChatRequestDTO DTO to a Message entity.
     * Ignores complex object fields (conversation) as they cannot be directly mapped from IDs.
     *
     * @param chatRequestDTO The ChatRequestDTO DTO to map.
     * @return The mapped Message entity.
     */
    @Mapping(target = "conversation", ignore = true)
    @Mapping(source = "input", target = "content")
    Message toEntityRequest(ChatRequestDTO chatRequestDTO);

    /**
     * Maps a ChatResponseDTO DTO to a Message entity.
     * Ignores complex object fields (conversation) as they cannot be
     * directly mapped from IDs.
     *
     * @param chatResponseDTO The ChatResponseDTO DTO to map.
     * @return The mapped Message entity.
     */
    @Mapping(target = "conversation", ignore = true)
    @Mapping(source = "response", target = "content")
    Message toEntityResponse(ChatResponseDTO chatResponseDTO);



}
