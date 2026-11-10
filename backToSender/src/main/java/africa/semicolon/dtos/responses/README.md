# `dtos.responses`

Response DTOs describe the data we **send back** to the client. We choose exactly which fields are shown, and in what format.

## `ApiResponse`

Every endpoint wraps its result in this one envelope, so clients always know what to expect.

```java
public class ApiResponse {
    private final boolean success;
    private final Object data;
    // constructor and getters
}
```

| Field | Meaning |
|---|---|
| `success` | `true` when the request worked, `false` when it failed. |
| `data` | The result on success (any object), or the error message on failure. |

Success:

```json
{
  "success": true,
  "data": { "trackingNumber": "K3D9QZ", "...": "..." }
}
```

Failure:

```json
{
  "success": false,
  "data": "Package with tracking number K3D9QZ not found"
}
```

`data` is typed as `Object` on purpose, to keep things simple for now.

## `SendDeliveryResponse`

Returned after a delivery is created.

```java
@Data
public class SendDeliveryResponse {
    private String trackingNumber;
    private String dateSent;
    private String sender;
    private String recipient;
    private String description;
}
```

`dateSent` is a ready-to-show string such as `Mon, 02-11-2026, 01:58,am`, built in `DeliveryServiceImpl`.

## `TrackDeliveryResponse`

Returned when someone tracks a delivery or adds a tracking event.

```java
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
```

- It holds a **list** of events. Each event is a small nested class (`TrackingHistoryResponse`).
- `PrettyTime` is a library that turns a date into friendly text such as `"3 minutes ago"`. The client gets readable text and not a raw timestamp.

Example:

```json
{
  "success": true,
  "data": {
    "trackingNumber": "K3D9QZ",
    "trackingHistoryResponses": [
      { "event": "Package Recieved at Sending Office", "eventDate": "2 minutes ago" },
      { "event": "Out for delivery", "eventDate": "moments ago" }
    ]
  }
}
```

## Notes

- `@Data` (Lombok) creates getters and setters. Spring uses the getters to write the JSON.
- Responses are built in the **service** layer from models and are never the models themselves.
