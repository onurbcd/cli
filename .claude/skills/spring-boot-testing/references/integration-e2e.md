# Integration / e2e tests (`*IT`, Failsafe)

Use only for what slices and unit tests cannot prove: full wiring, transactions across layers, Flyway migrations on a real PostgreSQL, security filter chain end to end, real serialization through HTTP.

## Base configuration

One shared, singleton container, wired with `@ServiceConnection`; no per-class containers, no `@DirtiesContext`.

```java
@TestConfiguration(proxyBeanMethods = false)
public class PostgresContainerConfig {
    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgres() {          // Boot 3.5+/4: org.testcontainers.postgresql.PostgreSQLContainer may apply; follow the compile result
        return new PostgreSQLContainer<>("postgres:17-alpine").withReuse(true);
    }
}
```

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)   // NONE for non-web apps (e.g. Spring Shell)
@Import(PostgresContainerConfig.class)
@ActiveProfiles("test")
abstract class AbstractIntegrationTest { }
```

- Same context configuration across classes → Spring caches the context. Avoid `@MockitoBean` sprinkled differently per class (each new combination creates a new context); group mocks in the base class or a few shared variants.
- `application-test.yaml` (or `src/test/resources/application.properties`): test-only secrets, `ddl-auto: validate`, Flyway enabled, root log level `WARN`. No real credentials, no local absolute paths: use `@TempDir` or `@DynamicPropertySource` for paths.
- Fallback when `@ServiceConnection` is unavailable: `@DynamicPropertySource` registering `spring.datasource.url/username/password` from a static singleton container started in a `static {}` block.

## Test data and isolation

- Each test creates what it needs (via `TestFactory` + repositories or `@Sql`) and cleans up: `@Transactional` rollback for same-thread flows, or `@Sql(executionPhase = AFTER_TEST_METHOD)` / explicit `deleteAll()` in `@AfterEach` when the code under test commits in another thread/transaction.
- Never rely on test order or on data left by another test. Use unique values per test (random UUID suffix) for unique-constrained fields.

## What to cover

1. **Migrations**: context starts with `ddl-auto=validate` (proves entities match the Flyway schema); a test that checks `flyway_schema_history` has no failed rows; data migrations assert resulting rows.
2. **End-to-end flow per use case**: request in → DB state asserted via repository → response out. Also the negative flows (validation error, not found, conflict) and that nothing was persisted (rollback).
3. **Transactions**: a failure in the second write rolls back the first.
4. **Security**: unauthenticated / wrong role / right role through the real filter chain; for JWT, sign tokens with the project's real `JwtService` or a test key; for Keycloak-style IdPs, `testcontainers-keycloak` (singleton) only when the code truly depends on it.
5. **External HTTP dependencies**: WireMock (or `MockRestServiceServer`/`RestTestClient` mock server) with exact request verification (method, path, headers, body) and error/timeout stubs. Never call real services.
6. **Messaging/async**: bounded waits with Awaitility (`await().atMost(5, SECONDS)`), never `Thread.sleep`.
7. **Time**: inject a fixed `Clock` bean in the test config.

## Web client by Boot version

- Boot 3.x: `TestRestTemplate` or `MockMvc` (`@AutoConfigureMockMvc`) or `WebTestClient`.
- Boot 4.x: `RestTestClient` (works with MockMvc or a live server; needs `@AutoConfigureRestTestClient` on slices), `MockMvcTester`.

Assert status, headers, body (recursive comparison), and the DB side effect.

## Non-web applications (Spring Shell, batch)

`@SpringBootTest(webEnvironment = NONE)`; invoke the command bean or runner method directly, assert returned text/exit code and DB state. See `spring-shell.md`.

## Naming and run

`*IT` classes run in Failsafe (`./mvnw verify`). They need Docker; say so in the report when Docker is unavailable instead of skipping silently.
