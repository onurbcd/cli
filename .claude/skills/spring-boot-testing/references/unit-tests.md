# Unit tests (Mockito + AssertJ)

Based on the Sientia reference repos (common-lib, mlops-bff, user-api), with the fixes listed in SKILL.md.

## Skeleton

```java
@ExtendWith(MockitoExtension.class)
class BillServiceTest {

    @Mock private BillRepository repository;
    @Mock private BillToEntityMapper mapper;
    @Captor private ArgumentCaptor<Bill> billCaptor;
    @InjectMocks private BillService service;

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("when bill is new, should map, persist and return the generated id")
        void givenNewBill_whenSave_shouldPersistAndReturnId() {
            var dto = TestFactory.createBillSaveDto();
            var entity = TestFactory.createBill();
            when(mapper.apply(dto)).thenReturn(entity);
            when(repository.save(entity)).thenReturn(entity);

            var result = service.save(dto, null);

            assertThat(result).isEqualTo(entity.getId());
            verify(mapper).apply(dto);
            verify(repository).save(billCaptor.capture());
            assertThat(billCaptor.getValue()).usingRecursiveComparison().isEqualTo(entity);
            verifyNoMoreInteractions(repository, mapper);
        }

        @Test
        @DisplayName("when repository fails, should propagate and not map twice")
        void givenRepositoryFailure_whenSave_shouldPropagate() {
            var dto = TestFactory.createBillSaveDto();
            when(mapper.apply(dto)).thenReturn(TestFactory.createBill());
            when(repository.save(any())).thenThrow(new DataIntegrityViolationException("dup"));

            assertThatThrownBy(() -> service.save(dto, null))
                    .isInstanceOf(DataIntegrityViolationException.class);

            verify(mapper).apply(dto);
            verify(repository).save(any(Bill.class));
            verifyNoMoreInteractions(repository, mapper);
        }
    }
}
```

## What to cover for every public (and package-visible) method

1. **Happy path** for each distinct branch outcome.
2. **Each condition operand**: for `a && b`, a case where `a` is false and one where `a` is true and `b` is false/true (JaCoCo counts each as a branch).
3. **Boundaries**: null, empty collection/string, blank, zero, negative, min/max, first/last page, duplicate.
4. **Optional/stream paths**: present, empty, multiple elements.
5. **Collaborator throws**: checked and unchecked; assert propagation or translation (type + business code + args).
6. **Side effects**: calls with exact arguments, arguments captured when the SUT builds the object, mutated parameters (assert on the original instance after the call).
7. **No-call guarantees**: `verifyNoInteractions` for collaborators that must be skipped on that path.
8. **Constructors / Lombok / records**: do not write tests that only read private fields. Cover them through behavior. If a constructor has logic, test that logic.
9. **Enums with behavior**: `@EnumSource` + a mapping test per constant that has logic (e.g. `fromCode`, lookups with a miss → exception).
10. **Static calls**: `try (var mocked = mockStatic(X.class)) { mocked.when(X::m).thenReturn(...); ...; mocked.verify(X::m); }`. Prefer injecting a `Clock`/provider over static mocking when you may change production code; otherwise mock statics.
11. **Time and randomness**: fixed `Clock.fixed(...)`; never assert on `now()` ranges.

## Exceptions

```java
assertThatThrownBy(() -> service.getById(id))
        .isInstanceOfSatisfying(ApiException.class, e -> {
            assertThat(e.getError()).isEqualTo(Error.NOT_FOUND);
            assertThat(e.getArgs()).containsExactly(id.toString());
        });
```

Assert the project's business error (enum/code/args), not the message string. Use `assertThatCode(...).doesNotThrowAnyException()` for "must not throw" paths.

## Parameterized tests

Use when two or more tests differ only by data.

```java
static Stream<Arguments> invalidInputs() {
    return Stream.of(
            Arguments.of("null name", (Consumer<BillSaveDto>) d -> d.setName(null), "name"),
            Arguments.of("blank name", (Consumer<BillSaveDto>) d -> d.setName(" "), "name"));
}

@ParameterizedTest(name = "[{index}] {0}")
@MethodSource("invalidInputs")
void givenInvalidDto_whenValidate_shouldReportField(String label, Consumer<BillSaveDto> mutate, String field) { ... }
```

Large matrices shared by several classes go in `TestArguments` as nested `ArgumentsProvider` classes.

## Helpers (`TestFactory`, `TestConstants`, `TestArguments`)

- `final`, private constructor (`@NoArgsConstructor(access = AccessLevel.PRIVATE)` if Lombok is on).
- `TestFactory.createX()` returns a valid, fully populated object (use the Lombok builder); tests mutate it for invalid variants. Fixed UUIDs/dates, no randomness unless the test is about uniqueness.
- No magic literals in tests: put reusable values in `TestConstants`.

## MapStruct mappers

Instantiate the generated `*MapperImpl` directly (or `Mappers.getMapper`). Assert every target field (`usingRecursiveComparison().isEqualTo(expected)`), null input → null output, null nested objects, collections empty/null, enums, `@Mapping(expression=...)` helpers. If the mapper `uses` other mappers/components, inject them with `ReflectionTestUtils.setField` or a Spring slice (`@SpringJUnitConfig` with only the needed beans).

## Validators and utilities

One case per rule and per boundary; for Jakarta constraints use a real `Validator` (`Validation.buildDefaultValidatorFactory()`) and assert `ConstraintViolation` property path + message key.

## Anti-patterns (reject in review)

- Tests asserting only `verify(...)` with no result assertion, or only `assertNotNull`.
- `any()` where the exact argument is knowable.
- `lenient()`, `@MockitoSettings(strictness = LENIENT)`, `@Disabled`.
- `RETURNS_DEEP_STUBS` unless the API is a fluent builder you do not own (e.g. Spring Shell `ComponentFlow.Builder`); then keep it local to that mock.
- Mocking value objects, DTOs or collections: build real ones.
- Testing the framework (Lombok getters, Spring annotations) instead of behavior.
- Using `@SpringBootTest` for something a Mockito test proves.
