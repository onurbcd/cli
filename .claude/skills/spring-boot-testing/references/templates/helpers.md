# Helper templates (copy into `src/test/java/<base package>/`)

Replace `com.acme.app` with the project's base package and the sample types with real ones.

## TestConstants

```java
package com.acme.app;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestConstants {
    public static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final String NAME = "name";
    public static final String EMAIL = "user@test.com";
    public static final String ROLE_USER = "USER";
}
```

## TestFactory (object mother)

```java
package com.acme.app;

import static com.acme.app.TestConstants.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestFactory {

    /** Valid, fully populated. Tests mutate a copy to build invalid variants. */
    public static BillSaveDto createBillSaveDto() {
        return BillSaveDto.builder().name(NAME).build();
    }

    public static Bill createBill() {
        return Bill.builder().id(ID).name(NAME).build();
    }
}
```

Deterministic values only (fixed UUIDs, `LocalDate.of(...)`, `Clock.fixed(...)`).

## TestArguments (shared parameter matrices)

```java
package com.acme.app;

import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public final class TestArguments {
    private TestArguments() {}

    public static class BillSaveDtoInvalid implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of("null name", (Consumer<BillSaveDto>) d -> d.setName(null), "name"),
                    Arguments.of("blank name", (Consumer<BillSaveDto>) d -> d.setName(" "), "name"));
        }
    }
}
```

Use with `@ParameterizedTest(name = "[{index}] {0}") @ArgumentsSource(TestArguments.BillSaveDtoInvalid.class)`.

## Surefire/Failsafe + test properties

`src/test/resources/application-test.yaml`:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
logging:
  level:
    root: WARN
app:
  secret-key: test-only-key-not-a-secret
```
