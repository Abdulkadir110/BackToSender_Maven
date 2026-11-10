package africa.semicolon.data.repositories;

import africa.semicolon.data.models.TrackingHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TrackingHistories extends MongoRepository<TrackingHistory, String> {
    List<TrackingHistory> findByTrackingNumber(String trackingNumber);
}
