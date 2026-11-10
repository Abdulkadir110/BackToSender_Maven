# `africa.semicolon` (root package)

This package holds the **entry point** of the application: `Main.java`.

```java
@SpringBootApplication
@EnableMongoAuditing
public class Main {
    public static void main(String... args) {
        SpringApplication.run(Main.class, args);
    }
}
```

## What happens when `Main` runs

1. `SpringApplication.run(...)` starts Spring and the embedded web server (port 8080 by default).
2. Spring **scans** this package and every package below it looking for classes marked with `@Service`, `@RestController`, `@RestControllerAdvice`, and so on. These objects are called **beans**.
3. Spring connects to MongoDB and creates the repository implementations.
4. Spring **injects** (hands over) each bean to the classes that need it.

## Annotations

| Annotation | Meaning |
|---|---|
| `@SpringBootApplication` | Marks the starting class. It turns on component scanning and auto-configuration. |
| `@EnableMongoAuditing` | Turns on automatic fields such as `@CreatedDate` in `TrackingHistory`. Without it, `eventDate` would stay `null`. |

## Important rule

`Main` must sit in the **top-most package**. Spring only scans the package of `Main` and below, so a class placed outside `africa.semicolon` would never be found.

## The sub-packages

| Package | Layer | Job |
|---|---|---|
| `controllers` | Presentation | Receive HTTP requests, return HTTP responses |
| `services` | Business | Rules and logic |
| `data.repositories` | Data access | Read and write the database |
| `data.models` | Data | What is stored in the database |
| `dtos.requests` | Transfer | Data coming **into** the API |
| `dtos.responses` | Transfer | Data going **out** of the API |
| `exceptions` | Error handling | Custom errors and the global handler |
