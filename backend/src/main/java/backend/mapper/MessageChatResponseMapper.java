package backend.mapper;
import backend.dto.ChatResponseDTO;
import backend.model.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper interface for converting between Message entity and ChatResponseDTO DTO.
 * The 'componentModel = "spring"' makes it a Spring component, allowing for @Autowired injection.
 */
@Mapper(componentModel = "spring")
public interface MessageChatResponseMapper {

    /**
     * Maps a Message entity to a ChatResponseDTO DTO.
     *
     * @param message The Message entity to map.
     * @return The mapped ChatResponseDTO DTO.
     */
    @Mapping(source = "conversation.id", target = "conversationId")
    ChatResponseDTO toDTO(Message message);

    /**
     * Maps a ChatResponseDTO DTO to a Message entity.
     * Ignores complex object fields (conversation) as they cannot be directly mapped from IDs.
     *
     * @param chatResponseDTO The ChatResponseDTO DTO to map.
     * @return The mapped Message entity.
     */

    @Mapping(source = "tokensCompletion", target = "tokenCount")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "conversation", ignore = true)
    Message toMessage(ChatResponseDTO chatResponseDTO);

    /**
     * Maps a list of Message entities to a list of ChatResponseDTO DTOs.
     *
     * @param messageList The list of Message entities to map.
     * @return A list of mapped ChatResponseDTO DTOs.
     */
    List<ChatResponseDTO> toDTOList(List<Message> messageList);

    /**
     * Maps a list of ChatResponseDTO DTOs to a list of Message entities.
     *
     * @param chatResponseDTOList The list of ChatResponseDTO DTOs to map.
     * @return A list of mapped Message entities.
     */
    List<Message> toMessageList(List<ChatResponseDTO> chatResponseDTOList);


}
