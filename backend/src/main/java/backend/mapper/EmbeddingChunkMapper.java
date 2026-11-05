package backend.mapper;

import backend.dto.EmbeddingChunkDTO;
import backend.model.MessageChunk;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.List;

/**
 * MapStruct mapper for converting between EmbeddingChunkDTO and MessageChunk.
 * The Spring component model makes the mapper available for dependency injection.
 */
@Mapper(componentModel = "spring")
public interface EmbeddingChunkMapper {

    /**
     * Maps an embedding chunk DTO to a MessageChunk entity.
     * The message relationship and persistence-managed fields must be set by the service or entity lifecycle.
     *
     * @param embeddingChunkDTO the DTO containing the chunk content and embedding data
     * @return the mapped MessageChunk entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "message", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "active", ignore = true)
    MessageChunk toEntity(EmbeddingChunkDTO embeddingChunkDTO);

    /**
     * Maps a MessageChunk entity to an embedding chunk DTO.
     *
     * @param messageChunk the entity containing the chunk content and embedding data
     * @return the mapped embedding chunk DTO
     */
    EmbeddingChunkDTO toDTO(MessageChunk messageChunk);

    /**
     * Maps a list of embedding chunk DTOs to MessageChunk entities.
     *
     * @param embeddingChunkDTOList the DTOs to map
     * @return the mapped entities
     */
    List<MessageChunk> toEntityList(List<EmbeddingChunkDTO> embeddingChunkDTOList);

    /**
     * Maps a list of MessageChunk entities to embedding chunk DTOs.
     *
     * @param messageChunkList the entities to map
     * @return the mapped DTOs
     */
    List<EmbeddingChunkDTO> toDTOList(List<MessageChunk> messageChunkList);

    /**
     * Converts a list of boxed embedding values to the primitive array used by the entity.
     *
     * @param embedding the embedding values
     * @return the embedding as a primitive float array, or null when the input is null
     */
    default float[] toFloatArray(List<Float> embedding) {
        if (embedding == null) {
            return null;
        }

        float[] result = new float[embedding.size()];
        for (int index = 0; index < embedding.size(); index++) {
            result[index] = embedding.get(index);
        }
        return result;
    }

    /**
     * Converts the entity's primitive embedding array to a list for the DTO.
     *
     * @param embedding the primitive embedding values
     * @return the embedding as a list, or null when the input is null
     */
    default List<Float> toFloatList(float[] embedding) {
        if (embedding == null) {
            return null;
        }

        List<Float> result = new ArrayList<>(embedding.length);
        for (float value : embedding) {
            result.add(value);
        }
        return result;
    }
}
