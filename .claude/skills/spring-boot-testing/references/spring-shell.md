# Spring Shell commands (this repo: `com.onurbcd.cli.command`)

Commands extend `BaseCommand` (`crudService`, `ComponentFlow.Builder flowBuilder`, `ShellHelper`, `name`, `EruTable`) and delegate to services. `ShellHelper` formats output (ANSI colors, tables, JSON). Existing example: `src/test/java/com/onurbcd/cli/command/BalanceCommandTest.java`.

## Approach

Unit-test each command with Mockito, calling the command methods directly (no shell runtime). Integration (`*IT`, `webEnvironment = NONE`) only for the full flow against PostgreSQL.

```java
@ExtendWith(MockitoExtension.class)
class BillCommandTest {
    @Mock private BillService billService;
    @Mock private ShellHelper shellHelper;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS) private ComponentFlow.Builder flowBuilder;  // fluent builder you do not own
    private BillCommand command;

    @BeforeEach
    void setUp() { command = new BillCommand(billService, /* ... */ flowBuilder, shellHelper); }
}
```

## What to cover per command method

- **Delegation**: service called with the exact arguments (ids, `CommandParam`, filter objects, `PageRequest`). Capture and assert the built `Pageable` (`pageNumber - 1`, size, direction, properties) and the filter fields.
- **Output**: the exact string returned. Stub `shellHelper.success/warning/error/printJson/printTable` to return a marker and assert the command returns that marker for the right input (e.g. `SAVE_SUCCESS.formatted(name, id)`, `DELETE_SUCCESS`, `OPERATION_CANCELLED`).
- **Save flow** (`baseSave`): flow returns `null` → warning, service never called; validation violations → error string, service never called; success → service called with DTO and id, success message. Cover each pre-condition (`SOURCE_REQUIRED`, `CATEGORY_REQUIRED`, ...) as `ApiException` with its `Error` enum.
- **Interruption**: `flowBuilder.build().run()` throwing `RuntimeException(new InterruptedIOException())` → cancelled warning; other exceptions propagate.
- **Optional/default params**: `null`, defaults for page/size/direction/properties, each filter field alone.
- **`@ShellOption` / `@Command` metadata**: do not test annotations; test behavior. If a method has defaultValue logic in code, test it.
- **`preSaveFlow`**: returns the right `SaveFlowParam` subtype with the items and paths from the services/config.

## Rules specific to this repo

- Do not assert private fields with `ReflectionTestUtils.getField` (the existing `constructor_*` tests in `BalanceCommandTest` do this; replace them with behavior tests when touching that file).
- Replace JUnit `Assertions` with AssertJ when editing existing tests in this repo.
- `ShellHelper` itself is a real unit under test: build it with real formatters or mocks and assert the ANSI-wrapped strings, the table text (use `contains` on stable headers/rows), `PAGE_INFO` formatting, and each `CellMatchers` formatter path (`LocalDateTime`, `BigDecimal`, `Number`, `Codeable`).
- `FlowFactory`, `SaveDtoFactory`, `SaveFlowParamFactory`: test each `FlowType`/enum branch; `FlowFactory` builds `ComponentFlow` steps, so use the deep-stub builder and verify the step IDs/arguments passed.
- Files/storage (`StorageService`, `FileUtil`, `CryptoHelper`): use `@TempDir`, never real paths. Assert path containment and I/O failure branches (`IOException` → business error).
- Secrets (`CryptoHelper`): round-trip test plus tamper/invalid-key failure paths; use test-only keys from test properties.
- Jackson in this repo is Jackson 3 (`tools.jackson.databind`).
- DB-backed flows: `*IT` with PostgreSQL Testcontainers and Flyway; the existing `src/test/resources/application.properties` must stay free of real credentials.
