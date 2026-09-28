package backend.mapper;

import backend.dto.MessageDTO;
import backend.model.Message;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper interface for converting between Message entity and ChatRequestDTO DTO.
 * The 'componentModel = "spring"' makes it a Spring component, allowing for @Autowired injection.
 */
@Mapper(componentModel = "spring")
public interface MessageMapper {

    /**
     * Maps a Message entity to a MessageDTO DTO.
     *
     * @param message The Message entity to map.
     * @return The mapped MessageDTO DTO.
     */
    MessageDTO toDTO(Message message);

    /**
     * Maps a MessageDTO DTO to a Message entity.
     * Ignores complex object fields (conversation) as they cannot be directly mapped from IDs.
     *
     * @param messageDTO The MessageDTO DTO to map.
     * @return The mapped Message entity.
     */
    Message toEntity(MessageDTO messageDTO);

    /**
     * Maps a list of Message entities to a list of MessageDTO DTOs.
     *
     * @param messageList The list of Message entities to map.
     * @return A list of mapped MessageDTO DTOs.
     */
    List<MessageDTO> toDtoList(List<Message> messageList);

    /**
     * Maps a list of MessageDTO DTOs to a list of Message entities.
     *
     * @param messageDTOList The list of MessageDTO DTOs to map.
     * @return A list of mapped Message entities.
     */
    List<Message> toEntityList(List<MessageDTO> messageDTOList);

}
