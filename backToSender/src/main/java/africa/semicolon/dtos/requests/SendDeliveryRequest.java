package africa.semicolon.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class SendDeliveryRequest {
    private String name;
    @NotBlank(message = "Description must not be blank")
    private String description;
    @Positive(message = "Weight must be greater than zero")
    private double weight;
    @NotBlank(message = "Sender must not be blank")
    private String sender;
    @NotBlank(message = "Recipient must not be blank")
    private String recipient;
}
