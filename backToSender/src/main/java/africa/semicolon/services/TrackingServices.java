package africa.semicolon.services;

import africa.semicolon.dtos.requests.UpdateTrackingRequest;
import africa.semicolon.dtos.responses.TrackDeliveryResponse;

public interface TrackingServices {

    TrackDeliveryResponse trackDelivery(String trackingNumber);

    TrackDeliveryResponse updateTracking(String trackingNumber, UpdateTrackingRequest request);
}
