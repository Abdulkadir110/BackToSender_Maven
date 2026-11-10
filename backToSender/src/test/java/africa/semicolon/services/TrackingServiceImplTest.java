package africa.semicolon.services;

import africa.semicolon.data.repositories.Packages;
import africa.semicolon.data.repositories.TrackingHistories;
import africa.semicolon.dtos.requests.SendDeliveryRequest;
import africa.semicolon.dtos.requests.UpdateTrackingRequest;
import africa.semicolon.exceptions.BackToSenderException;
import africa.semicolon.exceptions.PackageNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TrackingServiceImplTest {

    @Autowired
    private TrackingServices trackingServices;

    @Autowired
    private DeliveryServices deliveryServices;

    @Autowired
    private Packages packages;

    @Autowired
    private TrackingHistories trackingHistories;

    @BeforeEach
    public void setUp() {
        packages.deleteAll();
        trackingHistories.deleteAll();
    }

    private String sendTestDelivery() {
        SendDeliveryRequest sendDeliveryRequest = new SendDeliveryRequest();
        sendDeliveryRequest.setName("Test Package");
        sendDeliveryRequest.setDescription("This is a test package");
        sendDeliveryRequest.setWeight(2.5);
        sendDeliveryRequest.setSender("John Doe");
        sendDeliveryRequest.setRecipient("Jane Smith");
        return deliveryServices.sendDelivery(sendDeliveryRequest).getTrackingNumber();
    }

    private UpdateTrackingRequest updateRequest(String details) {
        UpdateTrackingRequest request = new UpdateTrackingRequest();
        request.setDetails(details);
        return request;
    }

    @Test
    public void trackingServiceThrowsExceptionWhenTrackingNumberIsNotFoundTest(){
        assertThrows(PackageNotFoundException.class, ()-> trackingServices.trackDelivery("12345"));
    }

    @Test
    public void trackingServiceDoesNotThrowExceptionWhenTrackingNumberIsFoundTest(){
        String trackingNumber = sendTestDelivery();
        assertDoesNotThrow(()-> trackingServices.trackDelivery(trackingNumber));
        var trackDeliveryResponse = trackingServices.trackDelivery(trackingNumber);
        assertEquals(trackingNumber, trackDeliveryResponse.getTrackingNumber());
        assertEquals(1, trackDeliveryResponse.trackingHistoryResponses.size());
    }

    @Test
    public void updateTracking_trackingHistoryIsTwoTest(){
        String trackingNumber = sendTestDelivery();

        var response = trackingServices.updateTracking(trackingNumber, updateRequest("Out for delivery"));

        assertEquals(trackingNumber, response.getTrackingNumber());
        assertEquals(2, response.trackingHistoryResponses.size());
        assertEquals(2, trackingHistories.findByTrackingNumber(trackingNumber).size());
    }

    @Test
    public void updateTracking_trackingHistoryContainsNewDetailsTest(){
        String trackingNumber = sendTestDelivery();

        trackingServices.updateTracking(trackingNumber, updateRequest("  Out for delivery  "));

        var events = trackingServices.trackDelivery(trackingNumber).trackingHistoryResponses;
        assertTrue(events.stream().anyMatch(event -> event.getEvent().equals("Out for delivery")));
    }

    @Test
    public void updateTracking_multipleTimesTest(){
        String trackingNumber = sendTestDelivery();

        trackingServices.updateTracking(trackingNumber, updateRequest("In transit"));
        var response = trackingServices.updateTracking(trackingNumber, updateRequest("Delivered"));

        assertEquals(3, response.trackingHistoryResponses.size());
    }

    @Test
    public void updateTracking_doesNotAffectOtherDeliveriesTest(){
        String firstTrackingNumber = sendTestDelivery();
        String secondTrackingNumber = sendTestDelivery();

        trackingServices.updateTracking(firstTrackingNumber, updateRequest("In transit"));

        assertEquals(2, trackingServices.trackDelivery(firstTrackingNumber).trackingHistoryResponses.size());
        assertEquals(1, trackingServices.trackDelivery(secondTrackingNumber).trackingHistoryResponses.size());
    }

    @Test
    public void updateTracking_unknownTrackingNumberThrowsExceptionTest(){
        assertThrows(PackageNotFoundException.class,
                () -> trackingServices.updateTracking("UNKNOWN", updateRequest("In transit")));
        assertEquals(0, trackingHistories.count());
    }

    @Test
    public void updateTracking_blankDetailsThrowsExceptionTest(){
        String trackingNumber = sendTestDelivery();

        assertThrows(BackToSenderException.class,
                () -> trackingServices.updateTracking(trackingNumber, updateRequest("   ")));
        assertThrows(BackToSenderException.class,
                () -> trackingServices.updateTracking(trackingNumber, updateRequest(null)));
        assertEquals(1, trackingHistories.findByTrackingNumber(trackingNumber).size());
    }
}
