package backend.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Chat Request resources, used to send chat request information to the endpoints
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRequestDTO {

    @NotBlank(message = "Input cannot be blank")
    private String input;

}
