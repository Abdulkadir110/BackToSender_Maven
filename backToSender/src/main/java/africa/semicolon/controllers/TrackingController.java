package africa.semicolon.controllers;

import africa.semicolon.dtos.requests.UpdateTrackingRequest;
import africa.semicolon.dtos.responses.TrackDeliveryResponse;
import africa.semicolon.dtos.responses.ApiResponse;
import africa.semicolon.services.TrackingServices;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tracking")
public class TrackingController {
    private final TrackingServices trackingServices;

    public TrackingController(TrackingServices trackingServices) {
        this.trackingServices = trackingServices;
    }

    @GetMapping("/{trackingNumber}")
    public ResponseEntity<ApiResponse> trackDelivery(@PathVariable("trackingNumber") String trackingNumber) {
        TrackDeliveryResponse response = trackingServices.trackDelivery(trackingNumber);
        return ResponseEntity.ok(new ApiResponse(true, response));
    }

    @PostMapping("/{trackingNumber}")
    public ResponseEntity<ApiResponse> updateTracking(@PathVariable("trackingNumber") String trackingNumber,
                                                      @Valid @RequestBody UpdateTrackingRequest request) {
        TrackDeliveryResponse response = trackingServices.updateTracking(trackingNumber, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, response));
    }
}
