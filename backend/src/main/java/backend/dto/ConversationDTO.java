package backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;

/**
 * Data Transfer Object for Conversation resources, used to retrieve conversations information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationDTO {

    @NotBlank
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$", message = "ID must be a valid UUID v4 format")
    private String id;

    @Size(max = 40)
    private String title;

}
