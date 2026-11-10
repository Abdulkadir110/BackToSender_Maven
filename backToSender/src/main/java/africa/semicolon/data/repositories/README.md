# `data.repositories`

Repositories are the **only** place that talks to the database. Services ask a repository to save or find data, and never write database code themselves.

## The code

```java
public interface Packages extends MongoRepository<Delivery, String> { }
```

```java
public interface TrackingHistories extends MongoRepository<TrackingHistory, String> {
    List<TrackingHistory> findByTrackingNumber(String trackingNumber);
}
```

These are **interfaces** with no body. We never write a class that implements them. Spring creates the implementation for us when the application starts.

## Reading `MongoRepository<Delivery, String>`

- `Delivery`: the model this repository manages.
- `String`: the type of its `@Id` field.

## Methods we get for free

| Method | What it does |
|---|---|
| `save(obj)` | Inserts a new document, or updates it if the id already exists. Returns the saved object. |
| `findById(id)` | Finds one document by id (returns an `Optional`). |
| `findAll()` | Returns every document. |
| `count()` | Returns how many documents exist. |
| `deleteAll()` | Removes every document. Mainly used in tests. |

## A custom query: `findByTrackingNumber`

```java
List<TrackingHistory> findByTrackingNumber(String trackingNumber);
```

We did not write any query. Spring reads the **method name** and builds it:

```
find  By  TrackingNumber   ->   "find the documents where trackingNumber equals the argument"
```

The field name in the method (`TrackingNumber`) must match a field in the model (`trackingNumber`). A spelling mistake stops the application from starting, which is a good way to catch errors early.

## Where it is used

- `DeliveryServiceImpl` saves a `Delivery` and its first `TrackingHistory`.
- `TrackingServiceImpl` calls `findByTrackingNumber` to read a delivery's history and `save` to add a new event.

## Testing

`PackagesTest` uses `@DataMongoTest`, which starts only the database part of Spring. This makes it fast and focused on the repository.
