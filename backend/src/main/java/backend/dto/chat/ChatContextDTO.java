package backend.dto.chat;

import backend.dto.MessageDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object to send the context of a conversation with the actual summary of a conversation and the messages
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatContextDTO {

    private String currentSummary;

    /**
     * Max response tokens used by the model
     */
    private int maxCompletionTokens;

    @NotBlank(message = "Message list cannot be blank")
    private List<MessageDTO> messageDTOList;

}
