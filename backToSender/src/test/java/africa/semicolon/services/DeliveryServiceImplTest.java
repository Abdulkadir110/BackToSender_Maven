package africa.semicolon.services;

import africa.semicolon.data.repositories.Packages;
import africa.semicolon.data.repositories.TrackingHistories;
import africa.semicolon.dtos.requests.SendDeliveryRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
public class DeliveryServiceImplTest {

    @Autowired
    private DeliveryServices deliveryService;

    @Autowired
    private Packages packages;

    @Autowired
    private TrackingHistories trackingHistories;

    @Test
    public void sendDeliveryPackagesCountIsOneTest(){
        packages.deleteAll();
        SendDeliveryRequest sendDeliveryRequest = new SendDeliveryRequest();
        sendDeliveryRequest.setName("G-String");
        sendDeliveryRequest.setDescription("Pink Lanky G-String");
        sendDeliveryRequest.setWeight(0.12);
        sendDeliveryRequest.setSender("chibuzo");
        sendDeliveryRequest.setRecipient("winnie");

        deliveryService.sendDelivery(sendDeliveryRequest);

        assertEquals(1, packages.count());
    }

    @Test
    public void sendDeliveryPackage_TrackingHistoryIsOneTest(){
        packages.deleteAll();
        trackingHistories.deleteAll();
        SendDeliveryRequest sendDeliveryRequest = new SendDeliveryRequest();
        sendDeliveryRequest.setName("G-String");
        sendDeliveryRequest.setDescription("Pink Lanky G-String");
        sendDeliveryRequest.setWeight(0.12);
        sendDeliveryRequest.setSender("chibuzo");
        sendDeliveryRequest.setRecipient("winnie");

        deliveryService.sendDelivery(sendDeliveryRequest);
        assertEquals(1, packages.count());
        assertThat(trackingHistories.count(), is(1L));
    }

    @Test
    public void sendDelivery_responseContainsTrackingNumberTest(){
        packages.deleteAll();
        trackingHistories.deleteAll();
        SendDeliveryRequest sendDeliveryRequest = new SendDeliveryRequest();
        sendDeliveryRequest.setName("G-String");
        sendDeliveryRequest.setDescription("Pink Lanky G-String");
        sendDeliveryRequest.setWeight(0.12);
        sendDeliveryRequest.setSender("chibuzo");
        sendDeliveryRequest.setRecipient("winnie");

        var response = deliveryService.sendDelivery(sendDeliveryRequest);
        assertNotNull(response.getTrackingNumber());
        assertEquals(6, response.getTrackingNumber().length());
        assertEquals(sendDeliveryRequest.getSender(), response.getSender());
        assertEquals(sendDeliveryRequest.getRecipient(), response.getRecipient());
        assertEquals(sendDeliveryRequest.getDescription(), response.getDescription());
        assertEquals(1, packages.count());
        assertThat(trackingHistories.count(), is(1L));
        log.info(response.toString());

    }

}