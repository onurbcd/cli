# Version detection and conventions

Read `pom.xml` before writing anything. Do not assume; check the parent and properties.

| Item | Where to look |
|---|---|
| Java | `<java.version>` / `<maven.compiler.release>` |
| Spring Boot | `spring-boot-starter-parent` version (or `spring-boot-dependencies` import) |
| Maven | `.mvn/wrapper/maven-wrapper.properties`; always use `./mvnw` when present |
| JUnit / Mockito / AssertJ | managed by the Boot BOM; confirm with `./mvnw dependency:tree -Dincludes=org.junit.jupiter,org.mockito,org.assertj` |
| JaCoCo, Surefire, Failsafe, PIT | `<build><plugins>` and `<pluginManagement>` |
| Testcontainers | dependencies; BOM-managed in Boot |

## Matrix

| | Spring Boot 3.x (Java 17+/21) | Spring Boot 4.x (Java 17+; 21/25 recommended) |
|---|---|---|
| JUnit | 5 (`org.junit.jupiter`) | 6 (same `org.junit.jupiter` package names; check dependency tree) |
| Mockito | 5.x (inline mock maker default, `mockStatic` works) | 5.x |
| Mock a bean in Spring context | `@MockitoBean` / `@MockitoSpyBean` (Boot 3.4+); `@MockBean` is deprecated | `@MockitoBean` / `@MockitoSpyBean`; `@MockBean`/`@SpyBean` removed |
| Test starter | `spring-boot-starter-test` | `spring-boot-starter-test` plus per-technology test starters (e.g. `spring-boot-starter-webmvc-test`, `spring-boot-starter-data-jpa-test`) |
| Web test client | `MockMvc` / `MockMvcTester`; `TestRestTemplate` | `MockMvcTester`, `RestTestClient` (replaces `TestRestTemplate`) |
| `@AutoConfigureMockMvc` | `org.springframework.boot.test.autoconfigure.web.servlet` | moved to the web MVC test module (`org.springframework.boot.webmvc.test.autoconfigure`); add explicitly |
| Testcontainers wiring | `@ServiceConnection` (3.1+) | `@ServiceConnection` |

Boot 4 package and starter names above come from release notes and community write-ups. **Confirm against the Boot 4.0 migration guide and the project's compile errors before relying on an import.** When unsure, compile (`./mvnw -q test-compile`) and fix imports from the real classpath.

## Rules of thumb

- Match the project's JUnit major. Do not add JUnit 4 or `junit-vintage`.
- Use `var`, records, text blocks, `switch` expressions when the Java release allows.
- Prefer constructor-injected SUTs: `@InjectMocks` for plain Mockito, or `new Sut(mocks...)` in `@BeforeEach` when there is more than one constructor or `@InjectMocks` would silently pick the wrong one.
- If the project's parent is not Boot (plain Spring), skip Boot-only annotations and use `@ExtendWith(SpringExtension.class)` + `@ContextConfiguration`.
- Surefire runs `*Test`; Failsafe runs `*IT`. If Failsafe is absent, add it (see `coverage-and-quality.md`) rather than naming integration tests `*Test`.
- Java 25 / newest JDK: if Mockito or ByteBuddy complains about unsupported class file versions, set `-Dnet.bytebuddy.experimental=true` in surefire `argLine` only as a last resort and tell the user.
