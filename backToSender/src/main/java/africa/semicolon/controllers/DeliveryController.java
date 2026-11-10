package africa.semicolon.controllers;

import africa.semicolon.dtos.requests.SendDeliveryRequest;
import africa.semicolon.dtos.responses.ApiResponse;
import africa.semicolon.dtos.responses.SendDeliveryResponse;
import africa.semicolon.services.DeliveryServices;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {
    private final DeliveryServices deliveryServices;

    public DeliveryController(DeliveryServices deliveryServices) {
        this.deliveryServices = deliveryServices;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> sendDelivery(@Valid @RequestBody SendDeliveryRequest request) {
        SendDeliveryResponse response = deliveryServices.sendDelivery(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, response));
    }
}
