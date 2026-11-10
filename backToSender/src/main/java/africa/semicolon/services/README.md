# `services`

The service layer holds the **business logic**: the rules of the application. Controllers call services, and services call repositories.

## Interface and implementation

Each service has two files:

| Interface (the contract) | Implementation (the work) |
|---|---|
| `DeliveryServices` | `DeliveryServiceImpl` |
| `TrackingServices` | `TrackingServiceImpl` |

Callers depend on the **interface**. This lets us replace the real service with a fake (a Mockito mock) in controller tests, and change the implementation later without touching the controllers.

## Annotations

| Annotation | Meaning |
|---|---|
| `@Service` | Tells Spring "create one object of this class and manage it". |
| `@Autowired` | Tells Spring "give me the object I need". This is **dependency injection**: the class never creates its own repositories with `new`. |

## `DeliveryServices`

```java
SendDeliveryResponse sendDelivery(SendDeliveryRequest sendDeliveryRequest);
```

What `DeliveryServiceImpl.sendDelivery` does, step by step:

1. Copy the request fields into a new `Delivery`.
2. Generate a random 6-character tracking number (letters `A-Z` and digits `0-9`).
3. Create the first `TrackingHistory` with the text `"Package Recieved at Sending Office"`.
4. Save the history and the delivery using the repositories.
5. Build and return a `SendDeliveryResponse`, with the date formatted as text.

```java
delivery.setTrackingNumber(generateTrackingNumber(6));
TrackingHistory savedTrackingHistory = trackingHistories.save(trackingHistory);
Delivery savedPackage = packages.save(delivery);
```

## `TrackingServices`

```java
TrackDeliveryResponse trackDelivery(String trackingNumber);
TrackDeliveryResponse updateTracking(String trackingNumber, UpdateTrackingRequest request);
```

### `trackDelivery`

```java
List<TrackingHistory> trackingHistoryList = trackingHistories.findByTrackingNumber(trackingNumber);
if (trackingHistoryList.isEmpty())
    throw new PackageNotFoundException("Package with tracking number " + trackingNumber + " not found");
return buildResponse(trackingNumber, trackingHistoryList);
```

1. Find every event for the tracking number.
2. If there are none, the package does not exist, so throw `PackageNotFoundException` (the global handler turns it into a 404).
3. Otherwise convert each event into a response object and return them all.

### `updateTracking`

```java
if (trackingHistories.findByTrackingNumber(trackingNumber).isEmpty())
    throw new PackageNotFoundException(...);
if (request == null || request.getDetails() == null || request.getDetails().isBlank())
    throw new BackToSenderException("Tracking details must not be blank");

TrackingHistory trackingHistory = new TrackingHistory();
trackingHistory.setTrackingNumber(trackingNumber);
trackingHistory.setDetails(request.getDetails().trim());
trackingHistories.save(trackingHistory);
return trackDelivery(trackingNumber);
```

1. Make sure the delivery exists, otherwise throw `PackageNotFoundException`.
2. Make sure the details are not blank, otherwise throw `BackToSenderException`. The controller already validates this, but the service checks again so it stays safe when called from somewhere else.
3. Save a new event. `eventDate` is filled in automatically.
4. Return the whole updated history.

## Rules of thumb

- Services never deal with HTTP (no status codes here). They throw exceptions and let the handler decide the status.
- Services never write database queries. They use repositories.
- Services receive and return DTOs, not raw HTTP objects.

## Testing

`DeliveryServiceImplTest` and `TrackingServiceImplTest` use `@SpringBootTest` with the real repositories and database. Each test clears the data first so tests do not affect each other.
