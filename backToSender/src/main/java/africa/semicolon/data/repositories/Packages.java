package africa.semicolon.data.repositories;

import africa.semicolon.data.models.Delivery;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface Packages extends MongoRepository<Delivery, String> {

}
