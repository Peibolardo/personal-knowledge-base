package backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object to send the actual summary of a conversation and the new messages to add to the summary
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConversationSummaryDTO {

    private String currentSummary;

    @NotBlank(message = "Message list cannot be blank")
    private List<MessageDTO> messageDTOList;

}
