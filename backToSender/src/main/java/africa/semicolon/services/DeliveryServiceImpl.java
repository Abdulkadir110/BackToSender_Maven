package africa.semicolon.services;

import africa.semicolon.data.models.Delivery;
import africa.semicolon.data.models.TrackingHistory;
import africa.semicolon.data.repositories.Packages;
import africa.semicolon.data.repositories.TrackingHistories;
import africa.semicolon.dtos.requests.SendDeliveryRequest;
import africa.semicolon.dtos.responses.SendDeliveryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class DeliveryServiceImpl implements DeliveryServices {
    private Packages packages;
    private TrackingHistories trackingHistories;

    @Autowired
   public DeliveryServiceImpl(Packages packages, TrackingHistories trackingHistories) {
       this.packages = packages;
       this.trackingHistories = trackingHistories;
   }
    @Override
    public SendDeliveryResponse sendDelivery(SendDeliveryRequest sendDeliveryRequest){
        Delivery delivery = new Delivery();
        delivery.setDescription(sendDeliveryRequest.getDescription());
        delivery.setWeight(sendDeliveryRequest.getWeight());
        delivery.setSender(sendDeliveryRequest.getSender());
        delivery.setRecipient(sendDeliveryRequest.getRecipient());
        delivery.setTrackingNumber(generateTrackingNumber(6));

        TrackingHistory trackingHistory = new TrackingHistory();
        trackingHistory.setTrackingNumber(delivery.getTrackingNumber());
        trackingHistory.setDetails("Package Recieved at Sending Office");

        TrackingHistory savedTrackingHistory = trackingHistories.save(trackingHistory);
        Delivery savedPackage =  packages.save(delivery);
        SendDeliveryResponse response = new SendDeliveryResponse();
        response.setTrackingNumber(savedPackage.getTrackingNumber());
        response.setDateSent(DateTimeFormatter.ofPattern("EEE, dd-MM-yyyy, HH:mm,a").format(savedTrackingHistory.getEventDate()));
        response.setSender(savedPackage.getSender());
        response.setRecipient(savedPackage.getRecipient());
        response.setDescription(savedPackage.getDescription());
        return response;
    }

    private String generateTrackingNumber(int numberOfCharacters) {
        char[] chars = {
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
                'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
                'U', 'V', 'W', 'X', 'Y', 'Z',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
        };
        StringBuilder trackingNumber = new StringBuilder();
        for (int i = 0; i < numberOfCharacters; i++) {
            int randomIndex = (int) (Math.random() * chars.length);
            trackingNumber.append(chars[randomIndex]);
        }
        return trackingNumber.toString();
    }
}
