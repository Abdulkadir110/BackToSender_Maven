package africa.semicolon.services;

import africa.semicolon.dtos.requests.SendDeliveryRequest;
import africa.semicolon.dtos.responses.SendDeliveryResponse;

public interface DeliveryServices {
    public SendDeliveryResponse sendDelivery(SendDeliveryRequest sendDeliveryRequest);
}
