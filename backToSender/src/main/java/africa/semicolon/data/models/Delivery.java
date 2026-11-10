package africa.semicolon.data.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class Delivery {
    @Id
    private String trackingNumber;
    private String name;
    private String description;
    private double weight;
    private String sender;
    private String recipient;
}
