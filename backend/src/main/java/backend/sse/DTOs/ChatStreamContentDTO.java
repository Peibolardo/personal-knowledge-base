package backend.sse.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * Data Transfer Object for Content Event Type for the SSE architecture.
 * Contains the content information from each stream chunk from the AI Service request.
 */
public class ChatStreamContentDTO {

    @NotBlank(message = "content can not be blank")
    private String content;

}



