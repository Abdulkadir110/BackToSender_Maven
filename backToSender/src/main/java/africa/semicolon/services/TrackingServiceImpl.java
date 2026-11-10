package africa.semicolon.services;

import africa.semicolon.data.models.TrackingHistory;
import africa.semicolon.data.repositories.TrackingHistories;
import africa.semicolon.dtos.requests.UpdateTrackingRequest;
import africa.semicolon.dtos.responses.TrackDeliveryResponse;
import africa.semicolon.exceptions.BackToSenderException;
import africa.semicolon.exceptions.PackageNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TrackingServiceImpl implements TrackingServices {
    @Autowired
    private TrackingHistories trackingHistories;

    @Override
    public TrackDeliveryResponse trackDelivery(String trackingNumber) {
        List<TrackingHistory> trackingHistoryList = trackingHistories.findByTrackingNumber(trackingNumber);
        if (trackingHistoryList.isEmpty())
            throw new PackageNotFoundException("Package with tracking number " + trackingNumber + " not found");
        return buildResponse(trackingNumber, trackingHistoryList);
    }

    @Override
    public TrackDeliveryResponse updateTracking(String trackingNumber, UpdateTrackingRequest request) {
        if (trackingHistories.findByTrackingNumber(trackingNumber).isEmpty())
            throw new PackageNotFoundException("Package with tracking number " + trackingNumber + " not found");
        if (request == null || request.getDetails() == null || request.getDetails().isBlank())
            throw new BackToSenderException("Tracking details must not be blank");

        TrackingHistory trackingHistory = new TrackingHistory();
        trackingHistory.setTrackingNumber(trackingNumber);
        trackingHistory.setDetails(request.getDetails().trim());
        trackingHistories.save(trackingHistory);
        return trackDelivery(trackingNumber);
    }

    private TrackDeliveryResponse buildResponse(String trackingNumber, List<TrackingHistory> trackingHistoryList) {
        TrackDeliveryResponse response = new TrackDeliveryResponse();
        response.setTrackingNumber(trackingNumber);
        List<TrackDeliveryResponse.TrackingHistoryResponse> trackingHistoryResponseList = new ArrayList<>();
        for (var trackingHistory : trackingHistoryList) {
            TrackDeliveryResponse.TrackingHistoryResponse trackingHistoryResponse = new TrackDeliveryResponse.TrackingHistoryResponse(trackingHistory.getDetails(), trackingHistory.getEventDate());
            trackingHistoryResponseList.add(trackingHistoryResponse);
        }
        response.setTrackingHistoryResponses(trackingHistoryResponseList);
        return response;
    }

}
