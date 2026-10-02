package backend.mapper;

import backend.dto.ConversationDTO;
import backend.model.Conversation;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper interface for converting between Conversation entity and ConversationDTO DTO.
 * The 'componentModel = "spring"' makes it a Spring component, allowing for @Autowired injection.
 */
@Mapper(componentModel = "spring")
public interface ConversationMapper {

    /**
     * Maps a ConversationDTO DTO to a Conversation entity.
     *
     * @param conversationDTO The ConversationDTO DTO to map.
     * @return The mapped Conversation entity.
     */
    Conversation toEntity(ConversationDTO conversationDTO);

    /**
     * Maps a Conversation entity to a ConversationDTO DTO.
     *
     * @param conversation The Conversation entity to map.
     * @return The mapped ConversationDTO DTO.
     */
    ConversationDTO toDTO(Conversation conversation);

    /**
     * Maps a list of Conversation entities to a list of ConversationDTO DTOs.
     *
     * @param conversationList The list of Conversation entities to map.
     * @return A list of mapped ConversationDTO DTOs.
     */
    List<ConversationDTO> toDTOList(List<Conversation> conversationList);

    /**
     * Maps a list of ConversationDTO DTOs to a list of Conversation entities.
     *
     * @param conversationDTOList The list of ConversationDTO DTOs to map.
     * @return A list of mapped Conversation entities.
     */
    List<Conversation> toEntityList(List<ConversationDTO> conversationDTOList);

}
