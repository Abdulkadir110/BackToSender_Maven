# `dtos.requests`

**DTO** means *Data Transfer Object*. A request DTO describes the data a client must **send to us**, usually as JSON.

## Why not use the model directly?

- A client should not be able to set fields it should not control, such as the `trackingNumber`.
- The API can change without changing the database, and the other way round.
- We can add validation rules in one place.

## `SendDeliveryRequest`

The body of `POST /api/deliveries`.

```java
@Data
public class SendDeliveryRequest {
    private String name;
    @NotBlank(message = "Description must not be blank")
    private String description;
    @Positive(message = "Weight must be greater than zero")
    private double weight;
    @NotBlank(message = "Sender must not be blank")
    private String sender;
    @NotBlank(message = "Recipient must not be blank")
    private String recipient;
}
```

Example JSON:

```json
{
  "name": "Parcel",
  "description": "Books",
  "weight": 2.5,
  "sender": "Ada",
  "recipient": "Bola"
}
```

## `UpdateTrackingRequest`

The body of `POST /api/tracking/{trackingNumber}`.

```java
@Data
public class UpdateTrackingRequest {
    @NotBlank(message = "Tracking details must not be blank")
    private String details;
}
```

Example JSON:

```json
{ "details": "Out for delivery" }
```

## Validation annotations

| Annotation | Rule |
|---|---|
| `@NotBlank` | The text must not be `null`, empty, or only spaces. |
| `@Positive` | The number must be greater than zero. |

The rules only run when the controller parameter is marked with `@Valid`:

```java
public ResponseEntity<ApiResponse> sendDelivery(@Valid @RequestBody SendDeliveryRequest request)
```

If a rule fails, Spring throws `MethodArgumentNotValidException`. The `GlobalExceptionHandler` catches it and returns **400 Bad Request** with the `message` text from the annotation, so the service is never called with bad data.

## `@Data`

This is a Lombok annotation. It generates the getters and setters that Spring needs in order to turn the JSON into a Java object, and that our code uses (`request.getDetails()`).
