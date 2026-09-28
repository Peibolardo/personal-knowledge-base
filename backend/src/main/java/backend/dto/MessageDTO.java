package backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Message resources, used to send Messages to the AI Service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDTO {

    @NotBlank(message = "content can not be blank")
    private String role;    // "user" or "assistant"

    @NotBlank(message = "content can not be blank")
    private String content;

}
