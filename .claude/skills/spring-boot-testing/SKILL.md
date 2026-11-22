---
name: spring-boot-testing
description: Creates complete JUnit/Mockito/AssertJ tests (unit, slice, integration/e2e with Testcontainers) for ONE Spring Boot class, or every class of ONE package, aiming at 100% line and branch coverage with every input, output, exception, collaborator call and mutated parameter verified. Use when asked to "criar testes", "testar a classe X", "aumentar cobertura", "unit test", "integration test", "teste e2e" or "JaCoCo" in a Java/Maven/Spring Boot project.
argument-hint: <ClassName | fully.qualified.ClassName | package.name>
allowed-tools: Read, Grep, Glob, Edit, Write, Bash(./mvnw *), Bash(mvn *), Bash(ls *), Bash(gh pr view *), Bash(gh pr checks *), Bash(gh pr list *), Bash(gh api repos/*), Bash(git branch --show-current)
---

# spring-boot-testing

Writes the tests for **one class or one package per run**, given as the argument (`$ARGUMENTS`). Target: 100% line and branch coverage of every target class, with strong assertions.

**Target resolution.** The argument is a simple class name, a fully qualified class name, or a package (`com.onurbcd.cli.config`, a path such as `config`, or a trailing `.*`). A package means the handwritten classes directly in that package; sub-packages are included only when the argument ends in `.**` (e.g. `com.onurbcd.cli.service.**`). The package form runs the workflow below once per class, so every class gets the full treatment.

## Hard rules

- Never run write git operations (commit, push, checkout, ...). Leave changes in the working tree.
- One class or one package per run. If `$ARGUMENTS` is empty, ambiguous or lists several unrelated targets, ask which one. Touch only `<Name>Test` (and `<Name>IT` when needed) of the target classes and shared test helpers.
- Package runs: each class goes through workflow steps 2-9 on its own, one after another (finish a class, gate included, before starting the next). Never batch several classes into one measurement and never lower the bar for a class because the package is large. Steps 10 and 11 run once, at the end.
- Never use `@Disabled`, `lenient()` or `@SuppressWarnings` to make a test pass. Never delete a failing assertion.
- Do not test private members via reflection. Exception: `ReflectionTestUtils.setField` for `@Value` fields.
- Never claim coverage or a green build you did not measure. Report real command output.
- The README "Automated Tests" section is the user-facing source of truth for commands and conventions; if it conflicts with this skill, tell the user and follow the README until resolved.
- **Never skip, merge or reorder workflow steps (1-11).** Every step is mandatory, including measuring (7), PIT (8) and locking in (9). Before reporting, walk the checklist below and confirm each step ran in this session. If a step truly cannot run, stop, say which step and why, and ask the user; never silently omit it or declare it "not configured" without having grepped `pom.xml`, `README.md` and `references/` for it first.
- Test code language: English (names, `@DisplayName`, comments). Talk to the user in their language.

## Workflow

0. **Pending Sonar issues (always first).** Before touching the target, check whether the current branch's PR has open SonarCloud/SonarQube issues: `gh pr list --head "$(git branch --show-current)"`, then `gh pr checks <n>` / `gh pr view <n> --comments` for the Sonar quality gate and its issue links. If Sonar issues are visible, list them and ask whether to fix them first (as part of this run, in the affected test files only). If they cannot be read (no PR, no token, private dashboard), ask the user to paste the pending issues instead of assuming there are none. Never skip this step silently.
1. **Resolve the target.** Class: glob `**/<Name>.java` under `src/main/java`; several hits: ask which; none: stop and say so. Package: list the `.java` files of that package under `src/main/java` (none: stop and say so). Drop what is not testable (generated code, `main`/bootstrap class, `package-info`, pure marker interfaces or annotations with no logic) and say what was dropped and why. Classes that already have a `<Name>Test` and are in the `jacoco-check` gate are skipped (list them); an existing test without the gate entry is reviewed and completed, not rewritten. Show the final ordered list (leaf classes with no project collaborators first). With more than ~8 classes, ask for confirmation before starting. For each class, read it and its direct collaborators' signatures (not their bodies).
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
8. **Strengthen (PIT, always).** Run PIT on the class every time; it is configured in `pom.xml` (`pitest-maven`):
   `./mvnw org.pitest:pitest-maven:mutationCoverage -Dpit.target.classes=<fqcn> -Dpit.target.tests='<fqcn>Test*'`.
   Kill every surviving mutant with stronger assertions, not with more mocks, then re-run until none survive (at minimum the `pit.mutation.threshold`; justify any survivor in the report). Report killed/total from the real output.
9. **Lock it in** (repos that use the `jacoco.check.includes` gate, like this one; the README documents it):
   - Confirm with `./mvnw verify -Djacoco.check.includes=<package/path>/<Name>.class`. It must pass.
   - Add `<package/path>/<Name>.class` as a new `<include>` in the `jacoco-check` execution of `pom.xml` (do not change anything else there). For a package run, add one Ant pattern for the whole package instead (`<package/path>/*.class`, or `<package/path>/**/*.class` with sub-packages), replacing any per-class includes it makes redundant.
   - Tick the matching item in `TODO.md` only if every item in that checkpoint is truly done and verified.
10. **Keep docs in sync.** If you change a command, property, convention or workflow described in the README "Automated Tests" section, or this skill's references, update both so they agree. Do not rewrite the README beyond that section.
11. **Report** (for a package, one block per class plus a summary table class × JaCoCo × PIT) in a short list: tests created, case matrix covered, commands run with real results (JaCoCo and PIT numbers), anything left uncovered and why, any production bug found (do not fix production code unless asked).

## Step completion checklist (confirm before reporting)

- [ ] 0 pending Sonar issues checked or user asked · [ ] 1 target resolved (class list confirmed for packages) · [ ] 2 stack detected · [ ] 3 class mapped · [ ] 4 case matrix built
- [ ] 5 level chosen · [ ] 6 tests written · [ ] 7 JaCoCo measured, 0 missed lines/branches
- [ ] 8 PIT run, no unjustified survivors · [ ] 9 gate include added and `verify` green
- [ ] 10 docs in sync · [ ] 11 report includes JaCoCo and PIT results
- Package runs: the checklist applies to **each** class; do not report until every class in the list is done or explicitly reported as blocked.

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
- **Sonar-clean tests (recurring PR issues).** Fix these while writing, not after the commit:
  - Never two test methods with identical bodies (Sonar S4144, "implementation identical to ..."), even if the inputs differ by data source. Merge them into one `@ParameterizedTest`: stack `@NullSource` / `@EmptySource` / `@ValueSource` / `@EnumSource` with `@MethodSource` on the same method (e.g. `@NullSource` + `@MethodSource("otherValues")` for "null or not the expected type"), or add the case to the existing `@MethodSource` provider. Keep a separate method only when its body or assertions really differ.
  - Before finishing each test class, scan it for methods whose bodies match (same calls, same assertions) and merge them.
  - Also avoid: unused imports/variables, empty or assertion-less tests, commented-out code, duplicated string literals (extract constants), `Thread.sleep`, and tests with several unrelated assertions of different behaviors.
- Repeated shapes → `@ParameterizedTest` (`@MethodSource`, `@ArgumentsSource`, `@EnumSource`, `@NullAndEmptySource`), with a readable `name`.
