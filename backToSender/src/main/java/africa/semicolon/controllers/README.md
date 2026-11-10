# `controllers`

Controllers are the **front door** of the application. They receive HTTP requests, hand the work to a service, and return an HTTP response. They contain no business logic.

## Endpoints

| Method | URL | Controller method | Success status |
|---|---|---|---|
| POST | `/api/deliveries` | `DeliveryController.sendDelivery` | `201 Created` |
| GET | `/api/tracking/{trackingNumber}` | `TrackingController.trackDelivery` | `200 OK` |
| POST | `/api/tracking/{trackingNumber}` | `TrackingController.updateTracking` | `201 Created` |

## `DeliveryController`

```java
@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {
    private final DeliveryServices deliveryServices;

    public DeliveryController(DeliveryServices deliveryServices) {
        this.deliveryServices = deliveryServices;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> sendDelivery(@Valid @RequestBody SendDeliveryRequest request) {
        SendDeliveryResponse response = deliveryServices.sendDelivery(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, response));
    }
}
```

Reading it from top to bottom:

1. The class handles every URL that starts with `/api/deliveries`.
2. Spring passes in a `DeliveryServices` through the **constructor**. This is called constructor injection.
3. `@RequestBody` turns the JSON body into a `SendDeliveryRequest`.
4. `@Valid` checks the rules in that request (see `dtos.requests`).
5. The service does the work.
6. We reply with status `201 Created` and the result wrapped in `ApiResponse`.

## `TrackingController`

```java
@GetMapping("/{trackingNumber}")
public ResponseEntity<ApiResponse> trackDelivery(@PathVariable("trackingNumber") String trackingNumber) {
    TrackDeliveryResponse response = trackingServices.trackDelivery(trackingNumber);
    return ResponseEntity.ok(new ApiResponse(true, response));
}

@PostMapping("/{trackingNumber}")
public ResponseEntity<ApiResponse> updateTracking(@PathVariable("trackingNumber") String trackingNumber,
                                                  @Valid @RequestBody UpdateTrackingRequest request) {
    TrackDeliveryResponse response = trackingServices.updateTracking(trackingNumber, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, response));
}
```

The same URL is used for two jobs. The **HTTP method** decides which one runs: `GET` reads the history and `POST` adds an event.

## Annotations

| Annotation | Meaning |
|---|---|
| `@RestController` | This class answers HTTP requests and its return values are written as JSON. |
| `@RequestMapping("/api/...")` | The base URL shared by all methods in the class. |
| `@GetMapping`, `@PostMapping` | The HTTP method and the rest of the URL for one Java method. |
| `@PathVariable` | Reads a value from the URL. In `/api/tracking/K3D9QZ`, `trackingNumber` is `K3D9QZ`. We write the name inside the annotation, `@PathVariable("trackingNumber")`, so it works without special compiler options. |
| `@RequestBody` | Converts the JSON body into a Java object. |
| `@Valid` | Runs the validation rules on that object. |

## `ResponseEntity`

`ResponseEntity` lets us choose the **HTTP status** and the **body** ourselves:

```java
ResponseEntity.ok(body)                                   // 200
ResponseEntity.status(HttpStatus.CREATED).body(body)      // 201
```

## Where do errors go?

Controllers do **not** catch exceptions. When a service throws one, Spring hands it to `GlobalExceptionHandler` (see `exceptions`), which applies to every controller:

| Problem | Status |
|---|---|
| Unknown tracking number | `404 Not Found` |
| Invalid or blank input | `400 Bad Request` |

## Testing

`DeliveryControllerTest` and `TrackingControllerTest` use **MockMvc** in standalone mode, together with a **Mockito mock** of the service:

```java
trackingServices = mock(TrackingServices.class);
mockMvc = standaloneSetup(new TrackingController(trackingServices))
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();
```

- No server and no database are started, so these tests are very fast.
- `when(...).thenReturn(...)` tells the fake service what to answer.
- `mockMvc.perform(get("/api/tracking/ABC123"))` pretends to send a request.
- `.andExpect(status().isOk())` and `.andExpect(jsonPath("$.success").value(true))` check the reply.
- `setControllerAdvice(...)` plugs in the global handler, because standalone mode does not find it on its own.

The tests check the success path, the 404 path, and the 400 validation path for each endpoint.
