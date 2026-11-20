# Assumptions

1. Only the content can be edited; the name is fixed at creation.
2. Name, content and reason must not be blank.
3. A reason is required only for reject and delete. The latest reason is stored on the request, and every reason is stored in the history.
4. Delete changes the state to `DELETED`; the record is not removed from the database.
5. `PUBLISHED`, `REJECTED` and `DELETED` are final states.
6. The publication number comes from a database sequence, so it is unique (but not necessarily gap-free).
7. Each action has its own endpoint. Delete is `POST /{id}/delete` because it takes a body with the reason.
8. The name filter is a case-insensitive "contains" match. The state filter is an exact match. Pages start at 0. The list is sorted newest first.
9. The database is in-memory H2, so the project runs without setup. The code has no database-specific SQL: publication numbers come from a JPA `@SequenceGenerator` (a `publication_numbers` table records each issued number), so switching databases only needs `spring.datasource.*` and a JDBC driver.
10. No authentication.
11. Concurrent changes to the same request are prevented with optimistic locking (`@Version`). The operation that commits second fails with `409 Conflict` and can be retried.
12. The domain model is separate from the JPA model. The `RequestRepository` interface uses Spring Data's `Page` and `Pageable` types for paging: they come from Spring Data Commons, not JPA, and avoid a custom paging type.
13. Request ids are UUIDs created by the domain when a request is created, so they are not guessable and do not depend on the database. A request that has no version yet is treated as new when it is saved.
