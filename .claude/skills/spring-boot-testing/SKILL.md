---
name: spring-boot-testing
description: Creates complete JUnit/Mockito/AssertJ tests (unit, slice, integration/e2e with Testcontainers) for ONE Spring Boot class, aiming at 100% line and branch coverage with every input, output, exception, collaborator call and mutated parameter verified. Use when asked to "criar testes", "testar a classe X", "aumentar cobertura", "unit test", "integration test", "teste e2e" or "JaCoCo" in a Java/Maven/Spring Boot project.
argument-hint: <ClassName | fully.qualified.ClassName>
allowed-tools: Read, Grep, Glob, Edit, Write, Bash(./mvnw *), Bash(mvn *), Bash(ls *)
---

# spring-boot-testing

Writes the tests for **one class per run**, given as the argument (`$ARGUMENTS`). Target: 100% line and branch coverage of that class, with strong assertions.

## Hard rules

- Never run write git operations (commit, push, checkout, ...). Leave changes in the working tree.
- One class per run. If `$ARGUMENTS` is empty or lists several classes, ask which one. Touch only `<Name>Test` (and `<Name>IT` when needed) and shared test helpers.
- Never use `@Disabled`, `lenient()` or `@SuppressWarnings` to make a test pass. Never delete a failing assertion.
- Do not test private members via reflection. Exception: `ReflectionTestUtils.setField` for `@Value` fields.
- Never claim coverage or a green build you did not measure. Report real command output.
- The README "Automated Tests" section is the user-facing source of truth for commands and conventions; if it conflicts with this skill, tell the user and follow the README until resolved.
- Test code language: English (names, `@DisplayName`, comments). Talk to the user in their language.

## Workflow

1. **Resolve the class.** Glob `**/<Name>.java` under `src/main/java`. Several hits: ask which. None: stop and say so. Read the class and its direct collaborators' signatures (not their bodies).
2. **Detect the stack.** Read the "Automated Tests" section of `README.md` (commands, gate, PIT, conventions) and `TODO.md` (current phase); follow them. Read `pom.xml` (Java release, Spring Boot parent, JUnit, Mockito, JaCoCo, Surefire/Failsafe, Testcontainers) and existing tests. Pick conventions from `references/versions.md`. Existing project conventions win over this skill's defaults, except the Hard rules.
3. **Map the class.** List: every branch (`if`, `switch`, ternary, `&&`/`||` operands, loops, `catch`, lambdas, `Optional` paths), every collaborator call, every thrown exception, every parameter that is mutated, every return path, static calls, `@Value`/config inputs.
4. **Build the case matrix** before writing code. One row per branch and per boundary (null, empty, blank, zero, max, invalid, collaborator throws). Columns: case, inputs, stubs, expected return/exception, expected calls (with arguments), expected state changes. Show it to the user when it has more than ~10 rows.
5. **Choose the level** (see `references/`):
   - Plain logic, services, mappers, validators, utils, commands → unit test, `references/unit-tests.md`.
   - Controllers, repositories, JSON, security rules → slice test, `references/slice-tests.md`.
   - Whole flows, Flyway, queries on real PostgreSQL, wiring → `*IT`, `references/integration-e2e.md`.
   - Spring Shell commands (this repo) → `references/spring-shell.md`.
   Default to the cheapest level that proves the behavior; add a higher level only for what a unit test cannot prove.
6. **Write the tests** with the templates in `references/templates/` (create `TestFactory`, `TestConstants`, `TestArguments` only if the project has none; extend them otherwise).
7. **Measure.** Run `./mvnw -q test -Dtest=<Name>Test` first, then `./mvnw verify` when `*IT` or JaCoCo is involved. Read `target/site/jacoco/jacoco.xml` (or `jacoco.csv`) for the target class: missed lines and branches. Add cases until both are zero. See `references/coverage-and-quality.md`. Allowed exclusions: generated code only (Lombok `@Generated`, MapStruct impls, QueryDSL `Q*`, `main`).
8. **Strengthen.** When PIT is configured, run it on the class; kill surviving mutants with stronger assertions, not with more mocks.
9. **Lock it in** (repos that use the `jacoco.check.includes` gate, like this one; the README documents it):
   - Confirm with `./mvnw verify -Djacoco.check.includes=<package/path>/<Name>.class`. It must pass.
   - Add `<package/path>/<Name>.class` as a new `<include>` in the `jacoco-check` execution of `pom.xml` (do not change anything else there).
   - Tick the matching item in `TODO.md` only if every item in that checkpoint is truly done and verified.
10. **Keep docs in sync.** If you change a command, property, convention or workflow described in the README "Automated Tests" section, or this skill's references, update both so they agree. Do not rewrite the README beyond that section.
11. **Report** in a short list: tests created, case matrix covered, commands run with real results, anything left uncovered and why, any production bug found (do not fix production code unless asked).

## Per-test checklist (all must hold)

- Return value asserted in full (`usingRecursiveComparison()` for objects, not just `isNotNull()`).
- Exceptions: type, business error code/args via `assertThatThrownBy(...).isInstanceOfSatisfying(...)`, never message text alone.
- Every stubbed collaborator call is `verify`'d with the exact arguments; objects built inside the SUT use `ArgumentCaptor` and their fields are asserted.
- `verifyNoMoreInteractions(...)` on every mock at the end; `verifyNoInteractions(...)` for mocks that must not be touched on that path.
- Arguments passed in are checked for mutation (compare against a copy/snapshot when the SUT could change them).
- Call order asserted with `InOrder` only when order is a business rule. Counts (`times(n)`) only when the count is a business rule.
- No shared mutable state, no `@Order`, no dependence on other tests.

## Naming and layout

- Package mirrors production. Class `<Name>Test`, package-private; `<Name>IT` for failsafe.
- Method: `given<Input>_when<Condition>_should<Result>` (drop `given`/`when` only when meaningless).
- `@Nested` class per method under test, `@DisplayName` in English on the nested class and on non-obvious tests.
- Blank lines separate arrange / act / assert / verify. No `// Arrange` comments. `var` for locals.
- AssertJ only. No JUnit `Assertions`, no Hamcrest.
- Repeated shapes → `@ParameterizedTest` (`@MethodSource`, `@ArgumentsSource`, `@EnumSource`, `@NullAndEmptySource`), with a readable `name`.
