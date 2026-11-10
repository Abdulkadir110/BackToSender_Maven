package africa.semicolon.data.repositories;

import africa.semicolon.data.models.Delivery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@DataMongoTest
public class PackagesTest {

    @Autowired
    private Packages packages;

    @Test
    public void testRepository() {
        packages.deleteAll();
        Delivery package1 = new Delivery();
        package1.setTrackingNumber("12345");
        package1.setName("Test Package");
        package1.setDescription("This is a test package");
        package1.setWeight(2.5);
        package1.setSender("John Doe");
        package1.setRecipient("Jane Smith");
        packages.save(package1);
        assertThat(packages.count(), is(1L));
    }
}