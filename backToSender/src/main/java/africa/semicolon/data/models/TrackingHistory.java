package africa.semicolon.data.models;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;



@Data
@Document
public class TrackingHistory {
    @Id
    private String id;
    private String trackingNumber;
    private String details;
    @CreatedDate
    private LocalDateTime eventDate;
}
