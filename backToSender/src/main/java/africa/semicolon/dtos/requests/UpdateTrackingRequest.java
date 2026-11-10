package africa.semicolon.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateTrackingRequest {
    @NotBlank(message = "Tracking details must not be blank")
    private String details;
}
