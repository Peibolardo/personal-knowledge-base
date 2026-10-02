package backend.mapper;
import backend.dto.ChatRequestDTO;
import backend.model.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper interface for converting between Message entity and ChatRequestDTO DTO.
 * The 'componentModel = "spring"' makes it a Spring component, allowing for @Autowired injection.
 */
@Mapper(componentModel = "spring")
public interface MessageChatRequestMapper {

    //MessageChatRequestMapper INSTANCE = Mappers.getMapper(MessageChatRequestMapper.class);

    /**
     * Maps a Message entity to a ChatRequestDTO DTO.
     *
     * @param message The Message entity to map.
     * @return The mapped ChatRequestDTO DTO.
     */
    @Mapping(source = "conversation.id", target = "conversationId")
    @Mapping(source = "content", target = "input")
    ChatRequestDTO toDTO(Message message);

    /**
     * Maps a ChatRequestDTO DTO to a Message entity.
     * Ignores complex object fields (conversation) as they cannot be directly mapped from IDs.
     *
     * @param chatRequestDTO The ChatRequestDTO DTO to map.
     * @return The mapped Message entity.
     */
    @Mapping(target = "conversation", ignore = true)
    @Mapping(source = "input", target = "content")
    Message toEntity(ChatRequestDTO chatRequestDTO);

    /**
     * Maps a list of Message entities to a list of ChatRequestDTO DTOs.
     *
     * @param messageList The list of Message entities to map.
     * @return A list of mapped ChatRequestDTO DTOs.
     */
    List<ChatRequestDTO> toDtoList(List<Message> messageList);

    /**
     * Maps a list of ChatRequestDTO DTOs to a list of Message entities.
     *
     * @param chatRequestDTOList The list of ChatRequestDTO DTOs to map.
     * @return A list of mapped Message entities.
     */
    List<Message> toEntityList(List<ChatRequestDTO> chatRequestDTOList);

}
