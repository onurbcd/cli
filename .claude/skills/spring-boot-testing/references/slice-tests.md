# Slice tests

Use the narrowest slice that proves the behavior. Imports differ between Boot 3 and Boot 4; see `versions.md` and fix them from the compile result.

| Target | Slice | Collaborators |
|---|---|---|
| REST controller, advice, validation, security rules on endpoints | `@WebMvcTest(XController.class)` | `@MockitoBean` for services |
| JPA repository, custom queries, QueryDSL predicates, entity mapping | `@DataJpaTest` + real PostgreSQL (Testcontainers) | none |
| JSON (de)serialization of DTOs | `@JsonTest` | none |
| Properties binding | `@SpringJUnitConfig` + `@EnableConfigurationProperties` + `TestPropertyValues`/`properties=` | none |
| Bean wiring of one `@Configuration` | `ApplicationContextRunner` | needed beans only |

## @WebMvcTest

```java
@WebMvcTest(BillController.class)
@Import(ApiExceptionHandler.class)
class BillControllerTest {

    @Autowired private MockMvcTester mvc;          // Boot 3.4+/4; or MockMvc
    @MockitoBean private BillService service;

    @Nested
    @DisplayName("GET /bills/{id}")
    class GetById {
        @Test
        @WithMockUser(roles = "USER")
        void givenExistingId_whenGet_shouldReturn200WithBody() {
            var dto = TestFactory.createBillDto();
            when(service.getById(dto.getId())).thenReturn(dto);

            assertThat(mvc.get().uri("/bills/{id}", dto.getId()))
                    .hasStatusOk()
                    .bodyJson().convertTo(BillDto.class).usingRecursiveComparison().isEqualTo(dto);

            verify(service).getById(dto.getId());
            verifyNoMoreInteractions(service);
        }
    }
}
```

Per endpoint, cover this matrix (drop rows that do not apply):

1. 401 unauthenticated; 403 wrong role (assert the resolved exception type or the error body).
2. 200/201/204 success: status, headers (`Location`), body fields, content type.
3. 400 invalid body: one parameterized case per constraint; assert the error payload shape (field + message key).
4. 400 malformed JSON, wrong type path variable (`MethodArgumentTypeMismatchException`), missing required param.
5. 404/409/422 from each business exception the service can throw (stub the service to throw; assert status + body).
6. Service called with the exact mapped arguments (`ArgumentCaptor`), no extra calls.
7. Pagination/sorting params: defaults and overrides reach the service as the right `Pageable`.
8. CSRF/CORS only when security config enables them.

Rules: no `@Order`; each test stubs its own data; do not boot the full context.

## @DataJpaTest (PostgreSQL)

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)   // never swap in H2
@Import(PostgresContainerConfig.class)                // @TestConfiguration with @ServiceConnection container
class BillRepositoryTest {

    @Autowired private BillRepository repository;
    @Autowired private TestEntityManager em;

    @Test
    void givenBillsOfTwoTypes_whenFindByType_shouldReturnOnlyMatching() {
        var a = em.persist(TestFactory.createBill());
        em.persist(TestFactory.createBill().setType(OTHER));
        em.flush(); em.clear();                         // force a real round trip

        var result = repository.findByType(a.getType());

        assertThat(result).extracting(Bill::getId).containsExactly(a.getId());
    }
}
```

Cover per repository method: found / not found / several, ordering, paging, constraint violation (unique, not null → `DataIntegrityViolationException`), soft-delete or auditing fields, derived and `@Query` methods, QueryDSL predicates (each filter field alone and combined, null filter fields ignored). Flyway migrations run automatically when Flyway is on the classpath, so the schema under test is the real one; keep `spring.jpa.hibernate.ddl-auto=validate` (or `none`) in test config.

`@DataJpaTest` is `@Transactional` and rolls back, so tests stay independent. `flush()` + `clear()` before asserting reads.

## @JsonTest

Serialize and deserialize every DTO with custom `@JsonFormat`, `@JsonProperty`, enums, null handling, unknown properties. Assert with `JacksonTester`/`JsonContent` (`hasJsonPathStringValue`, `extractingJsonPathValue`). Boot 4 uses Jackson 3 (`tools.jackson`); match the project's `ObjectMapper` package.

## Configuration / properties

`ApplicationContextRunner` with `.withPropertyValues(...)`: assert bean presence/absence for each `@Conditional*` branch, binding defaults, validation failures (`context.getStartupFailure()`).
