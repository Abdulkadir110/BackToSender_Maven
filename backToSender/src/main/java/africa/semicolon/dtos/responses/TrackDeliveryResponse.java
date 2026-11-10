package africa.semicolon.dtos.responses;

import lombok.Data;
import org.ocpsoft.prettytime.PrettyTime;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class TrackDeliveryResponse {
    private String trackingNumber;
    private String description;
    public List<TrackingHistoryResponse> trackingHistoryResponses = new ArrayList<>();

    @Data
    public static class TrackingHistoryResponse {
        private String event;
        private String eventDate;

        public TrackingHistoryResponse(String event, LocalDateTime eventDate) {
            this.event = event;
            this.eventDate = new PrettyTime().format(eventDate);
        }

    }
}
