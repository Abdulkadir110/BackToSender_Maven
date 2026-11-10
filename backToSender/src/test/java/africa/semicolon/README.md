# Tests

All tests use **JUnit 5**, and run with `mvn test`. The tests that touch the database need MongoDB running on `localhost:27017`.

```
africa/semicolon
  data/repositories/PackagesTest             repository layer
  services/DeliveryServiceImplTest           service layer
  services/TrackingServiceImplTest           service layer
  controllers/DeliveryControllerTest         controller layer
  controllers/TrackingControllerTest         controller layer
```

## Three kinds of test

| Kind | Annotation / tool | Real database? | Speed |
|---|---|---|---|
| Repository | `@DataMongoTest` | Yes | Medium |
| Service | `@SpringBootTest` | Yes | Slower (starts everything) |
| Controller | `MockMvc` + Mockito (no Spring context) | No | Very fast |

## Annotations and tools

| Name | Meaning |
|---|---|
| `@Test` | This method is a test. |
| `@BeforeEach` | Runs before every test. We use it to clear the database so tests are independent. |
| `@SpringBootTest` | Starts the whole application, so real beans can be injected. |
| `@DataMongoTest` | Starts only the repository part of the application. |
| `@Autowired` | Injects a bean into the test class. |
| `mock(X.class)` (Mockito) | Creates a fake `X` that does nothing until we tell it what to do. |
| `when(...).thenReturn(...)` | Tells a mock what to answer. |
| `when(...).thenThrow(...)` | Tells a mock to throw an exception. |
| `verify(mock).method(...)` | Checks that the method was called. |
| `verifyNoInteractions(mock)` | Checks the mock was never used, for example when validation rejects a request first. |
| `ArgumentCaptor` | Captures the object passed to a mock so we can inspect it. |
| `MockMvc` | Sends fake HTTP requests to a controller without starting a server. |
| `jsonPath("$.data.trackingNumber")` | Reads a value from the JSON reply. `$` is the root. |

## Arrange, Act, Assert

Every test follows three steps:

```java
// Arrange: prepare data
String trackingNumber = sendTestDelivery();

// Act: do the thing we are testing
var response = trackingServices.updateTracking(trackingNumber, updateRequest("Out for delivery"));

// Assert: check the result
assertEquals(2, response.trackingHistoryResponses.size());
```

## Service test example

Real objects, real database. We clear the data first:

```java
@BeforeEach
public void setUp() {
    packages.deleteAll();
    trackingHistories.deleteAll();
}
```

## Controller test example

A fake service and fake HTTP, no database:

```java
when(trackingServices.trackDelivery("MISSING"))
        .thenThrow(new PackageNotFoundException("Package with tracking number MISSING not found"));

mockMvc.perform(get("/api/tracking/MISSING"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false));
```

This checks that the controller and `GlobalExceptionHandler` turn the exception into a `404`. It does **not** test the service, because that is tested separately.

## What is covered

- **Repository:** saving a delivery.
- **Delivery service:** a delivery and its first tracking event are saved, and the response has the tracking number and details.
- **Tracking service:** tracking works, history grows with each update, details are trimmed, other deliveries are unaffected, and an unknown number or blank details throw.
- **Delivery controller:** `201` with the wrapped response, the request is passed to the service correctly, and invalid input returns `400`.
- **Tracking controller:** `200` for tracking, `201` for an update, `404` for an unknown number, and `400` for blank details.

## Tips for students

- Name tests after the behaviour they check.
- Test the failure cases as well as the success case.
- Keep tests independent: never rely on another test having run first.
