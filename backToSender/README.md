# BackToSender

A small Spring Boot + MongoDB project for teaching **Maven** and a **layered architecture**.
It lets a sender register a delivery, get a tracking number, and lets staff add tracking events.

## Prerequisites

- Java 26
- Maven 3.8+
- MongoDB running on `localhost:27017` (the default; the tests use it too)

## Maven

Maven reads `pom.xml` to know how to build the project.

| Part of `pom.xml` | Meaning |
|---|---|
| `groupId` / `artifactId` / `version` | The project's unique identity (its "coordinates"). |
| `<properties>` | Reusable values, e.g. `${spring-boot.version}` is used by several dependencies. |
| `<dependencies>` | Libraries Maven downloads for us (see below). |
| `maven-compiler-plugin` | Compiles the code and runs **Lombok** as an annotation processor. |
| `maven-surefire-plugin` | Finds and runs the tests (version 3.x is needed for JUnit 5). |

Dependencies used:

- `spring-boot-starter-web`: REST controllers and an embedded web server.
- `spring-boot-starter-data-mongodb`: talk to MongoDB through repositories.
- `spring-boot-starter-validation`: `@Valid`, `@NotBlank`, `@Positive`.
- `spring-boot-starter-test` / `spring-boot-starter-data-mongodb-test`: JUnit 5, Mockito, MockMvc, `@DataMongoTest`.
- `lombok`: generates getters/setters at compile time (`provided` scope = only needed to compile).
- `prettytime`: formats dates like "3 minutes ago".

Common commands:

```bash
mvn compile          # compile the code
mvn test             # compile and run all tests
mvn spring-boot:run  # not configured; run Main from your IDE instead
```

## Layered architecture

A request travels down the layers and the response travels back up. Each layer only talks to the one below it.

```
Client (HTTP)
   |
Controller   africa.semicolon.controllers   receives HTTP, returns HTTP
   |
Service      africa.semicolon.services      business rules
   |
Repository   africa.semicolon.data.repositories   database access
   |
Model        africa.semicolon.data.models   what is stored in MongoDB
```

Supporting packages:

- `dtos.requests` / `dtos.responses`: objects used to send data **in** and **out** of the API, so models are never exposed directly.
- `exceptions`: our own exceptions and the global handler.

## Package guide

### `data.models`
- `Delivery`: a package being sent (tracking number, description, weight, sender, recipient).
- `TrackingHistory`: one event in a delivery's journey (tracking number, details, `eventDate`).

### `data.repositories`
- `Packages` and `TrackingHistories` are **interfaces** extending `MongoRepository`. Spring generates the implementation, giving `save`, `findAll`, `count`, `deleteAll`, etc. for free.
- `findByTrackingNumber(...)` is a *derived query*: Spring builds the query from the method name.

### `services`
- `DeliveryServices` / `DeliveryServiceImpl`: creates a delivery, generates a 6-character tracking number, and saves the first tracking event.
- `TrackingServices` / `TrackingServiceImpl`:
  - `trackDelivery(trackingNumber)` returns the full history, or throws `PackageNotFoundException`.
  - `updateTracking(trackingNumber, request)` adds a new event to an existing delivery.

Interface + implementation lets controllers depend on the *contract*, not the details, which makes testing with mocks easy.

### `controllers`

| Method | URL | Purpose | Success status |
|---|---|---|---|
| POST | `/api/deliveries` | Send a delivery | 201 Created |
| GET | `/api/tracking/{trackingNumber}` | View tracking history | 200 OK |
| POST | `/api/tracking/{trackingNumber}` | Add a tracking event | 201 Created |

Every response uses the same envelope:

```json
{ "success": true,  "data": { "...": "..." } }
{ "success": false, "data": "Package with tracking number XYZ not found" }
```

Error statuses: `404` for an unknown tracking number, `400` for invalid input.

Example:

```bash
curl -X POST localhost:8080/api/deliveries -H "Content-Type: application/json" \
  -d '{"name":"Parcel","description":"Books","weight":2.5,"sender":"Ada","recipient":"Bola"}'

curl -X POST localhost:8080/api/tracking/ABC123 -H "Content-Type: application/json" \
  -d '{"details":"Out for delivery"}'

curl localhost:8080/api/tracking/ABC123
```

### `exceptions`
- `BackToSenderException`: base runtime exception for the app.
- `PackageNotFoundException`: extends it; means the tracking number does not exist.
- `GlobalExceptionHandler`: one place that turns exceptions into HTTP responses for **all** controllers.

## Annotations explained

| Annotation | Where | What it does |
|---|---|---|
| `@SpringBootApplication` | `Main` | Marks the app entry point; enables component scanning and auto-configuration. |
| `@EnableMongoAuditing` | `Main` | Lets `@CreatedDate` fill in dates automatically. |
| `@Document` | models | This class is stored as a MongoDB document. |
| `@Id` | models | The document's unique id field. |
| `@CreatedDate` | `TrackingHistory.eventDate` | Set automatically when first saved. |
| `@Data` (Lombok) | models, DTOs | Generates getters, setters, `toString`, `equals`, `hashCode`. |
| `@Service` | service implementations | Registers the class as a Spring bean in the service layer. |
| `@Autowired` | services | Asks Spring to inject a dependency (dependency injection). |
| `@RestController` | controllers | Class handles HTTP requests and returns JSON. |
| `@RequestMapping` | controllers | Base URL for the controller. |
| `@GetMapping` / `@PostMapping` | controller methods | Maps an HTTP method and path to a Java method. |
| `@PathVariable` | parameters | Reads a value from the URL, e.g. `{trackingNumber}`. |
| `@RequestBody` | parameters | Converts the JSON body into a Java object. |
| `@Valid` | parameters | Runs the validation annotations on the request object. |
| `@NotBlank`, `@Positive` | request DTOs | Validation rules; failures become a `400` response. |
| `@RestControllerAdvice` | `GlobalExceptionHandler` | Applies to every controller. |
| `@ExceptionHandler` | handler methods | Says which exception a method handles. |

Testing annotations:

| Annotation | What it does |
|---|---|
| `@Test` | Marks a test method (JUnit 5). |
| `@BeforeEach` | Runs before every test, used to reset state. |
| `@SpringBootTest` | Starts the full Spring context (real beans, real MongoDB). |
| `@DataMongoTest` | Starts only the MongoDB/repository part of the app. |
| `@Autowired` | Injects beans into the test class. |
| `@Slf4j` (Lombok) | Provides a `log` field. |

## Tests

```
src/test/java/africa/semicolon
  data/repositories/PackagesTest             repository layer (needs MongoDB)
  services/DeliveryServiceImplTest           service layer (needs MongoDB)
  services/TrackingServiceImplTest           service layer (needs MongoDB)
  controllers/DeliveryControllerTest         controller layer, service mocked
  controllers/TrackingControllerTest         controller layer, service mocked
```

- **Repository and service tests** use real beans and a real database, so each test clears data first.
- **Controller tests** use `MockMvc` in *standalone* mode with a **Mockito** mock of the service. They start no server or database and only check HTTP behaviour: URLs, status codes, JSON, validation and error handling. `GlobalExceptionHandler` is attached with `setControllerAdvice(...)`.

Each test follows **Arrange, Act, Assert**: set up data, call the code, verify the result.

## Suggested exercises

1. Add a `GET /api/deliveries/{trackingNumber}` endpoint with a matching service method and tests.
2. Add a `status` field (e.g. `RECEIVED`, `DELIVERED`) to `TrackingHistory`.
3. Reject updates to a delivery that is already delivered.
