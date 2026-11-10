# `exceptions`

This package holds our own **error types** and the single class that turns errors into HTTP responses.

## Custom exceptions

```java
public class BackToSenderException extends RuntimeException {
    public BackToSenderException(String message) { super(message); }
}

public class PackageNotFoundException extends BackToSenderException {
    public PackageNotFoundException(String message) { super(message); }
}
```

```
RuntimeException
  +-- BackToSenderException          general problem in our app
        +-- PackageNotFoundException   the tracking number does not exist
```

- They extend `RuntimeException`, so they are **unchecked**. We do not have to write `throws` everywhere.
- `PackageNotFoundException` **is a** `BackToSenderException`. This lets us handle all our errors together, or one kind on its own.

Where they are thrown (in the services):

```java
throw new PackageNotFoundException("Package with tracking number " + trackingNumber + " not found");
```

## `GlobalExceptionHandler`

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PackageNotFoundException.class)
    public ResponseEntity<ApiResponse> handlePackageNotFound(PackageNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse(false, exception.getMessage()));
    }

    @ExceptionHandler(BackToSenderException.class)
    public ResponseEntity<ApiResponse> handleBackToSender(BackToSenderException exception) { ... }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidation(MethodArgumentNotValidException exception) { ... }
}
```

| Exception | Status | Message in `data` |
|---|---|---|
| `PackageNotFoundException` | `404 Not Found` | The exception message |
| `BackToSenderException` (any other) | `400 Bad Request` | The exception message |
| `MethodArgumentNotValidException` (failed `@Valid`) | `400 Bad Request` | The first validation message |

Spring picks the **most specific** handler. A `PackageNotFoundException` goes to the 404 method, not the more general `BackToSenderException` one.

### Annotations

| Annotation | Meaning |
|---|---|
| `@RestControllerAdvice` | One class that watches **all** controllers and writes JSON replies. |
| `@ExceptionHandler(X.class)` | "This method handles exceptions of type `X`." |

### Why one global handler?

- We do not repeat `try/catch` in every controller.
- Every error has the same JSON shape (`success: false`).
- A new controller is covered automatically.

Example error reply:

```json
{
  "success": false,
  "data": "Package with tracking number K3D9QZ not found"
}
```

## Adding a new error (exercise)

1. Create `class DeliveryAlreadyCompletedException extends BackToSenderException`.
2. Throw it from a service when a rule is broken.
3. Optionally add a method with `@ExceptionHandler` to choose a different status, such as `409 Conflict`.
4. Test it in a controller test by making the mock service throw it.
