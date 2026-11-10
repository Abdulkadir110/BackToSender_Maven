package africa.semicolon.dtos.responses;

import lombok.Data;

@Data
public class SendDeliveryResponse {
    private String trackingNumber;
    private String dateSent;
    private String sender;
    private String recipient;
    private String description;
}
