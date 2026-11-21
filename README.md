# Eru Service

## Prerequisites

- Java 25 (the repository includes `.sdkmanrc` for SDKMAN; enable `sdkman_auto_env=true` to activate it automatically when entering the project directory).
- The Maven Wrapper is included. Use `./mvnw` to use the Maven version configured for this project.
- A local PostgreSQL instance to run the application.

Activate the project Java version and check the environment:

```bash
sdk env
java -version
./mvnw --version
```

## Development

```bash
./mvnw clean package       # compile and package
./mvnw test                # run tests
./mvnw verify              # verify the build and generate the JaCoCo report
./mvnw spring-boot:run     # start the CLI (requires PostgreSQL)
```

## Automated Tests

Automated tests check that the application's rules and integrations behave as expected. Unit tests exercise a class or small unit in isolation, making failures easier to locate and keeping feedback fast. Integration tests check boundaries such as persistence, database queries, and Flyway migrations against PostgreSQL.

Tests live under `src/test/java`, mirror the production package structure, and use the `*Test` suffix. JUnit Jupiter provides the test framework, Mockito isolates collaborators, and AssertJ provides readable assertions. Test objects are built with `TestFactory` helpers that return valid, fixed values; set the values that matter to a scenario explicitly so the test's intent stays clear.

Unit tests (`*Test`) run with `./mvnw test`. Integration tests (`*IT`) run in the `verify` phase (Failsafe), so use `./mvnw verify` to run everything:

```bash
./mvnw test      # unit tests only (fast)
./mvnw verify    # unit + integration tests, JaCoCo report and coverage check
```

PostgreSQL integration tests use Testcontainers and start a disposable PostgreSQL container. A Docker-compatible container runtime must be available. The test profile supplies isolated placeholder settings and uses the container connection, so tests do not require the runtime database credentials or local storage paths.

To run an individual test class, use its class name:

```bash
./mvnw -Dtest=NumberUtilTest test
```

### Coverage report and gate

`./mvnw verify` writes the JaCoCo report: HTML at `target/site/jacoco/index.html` (open it and click through packages to see which lines and branches are not covered) and XML at `target/site/jacoco/jacoco.xml` (used by Sonar). Unit and integration coverage go into the same report.

The goal is 100% line and branch coverage of handwritten production code, reached one class at a time. To avoid breaking the build for classes that have no tests yet, the build only enforces 100% on the classes matched by the `jacoco.check.includes` property in `pom.xml`. Its default matches nothing. To check a single class:

```bash
./mvnw verify -Djacoco.check.includes=com/onurbcd/cli/util/NumberUtil.class
```

The value is one class-file pattern: slashes, `.class` suffix, wildcards allowed. If the class is below 100% the build fails and prints which rule was violated. Once a class reaches 100%, add its pattern as an `<include>` in the `jacoco-check` execution of `pom.xml` so it stays protected, and then tick it off in `TODO.md`. Generated code (mapper implementations, QueryDSL `Q*` classes) and the application bootstrap are not part of the target.

### Mutation testing (PIT)

100% coverage proves the code ran, not that the tests would notice a bug. PIT changes the code in small ways (flips a condition, removes a call) and checks that some test fails; a change that no test notices is a "surviving mutant" and means an assertion is missing. It is slow, so it is not part of the normal build. Run it on one class:

```bash
./mvnw org.pitest:pitest-maven:mutationCoverage \
  -Dpit.target.classes=com.onurbcd.cli.util.NumberUtil \
  -Dpit.target.tests='com.onurbcd.cli.util.NumberUtilTest*'
```

The report is written to `target/pit-reports/index.html`. The build fails if fewer than 90% of mutants are killed (`pit.mutation.threshold`).

### Writing tests with the Claude Code skill

The project has a skill, `.claude/skills/spring-boot-testing`, that writes the tests for **one class per run**. In Claude Code, run:

```
/spring-boot-testing NumberUtil
```

The skill finds the class, reads `pom.xml` to match the project's versions, lists every branch, exception and collaborator call, writes `NumberUtilTest` (and an `*IT` when a real database is needed), measures coverage with JaCoCo until the class reaches 100%, runs PIT, and reports what it did. It never runs write git commands, so review and commit the result yourself. A typical session for one class:

1. `/spring-boot-testing <ClassName>`
2. `./mvnw verify -Djacoco.check.includes=<package/path>/<ClassName>.class` to confirm 100%
3. Run the PIT command above and fix any surviving mutant
4. Add the class to the permanent gate in `pom.xml` and update `TODO.md`

Test conventions used by the skill: method names as `given<Input>_when<Condition>_should<Result>`, a `@Nested` class per method under test, English `@DisplayName`, AssertJ assertions only, `verifyNoMoreInteractions` on every mock, and no dependence between tests.

## Postgres in Docker

```bash
docker run -p5432:5432 --name postgres \
  -e POSTGRES_PASSWORD=secret \
  -e POSTGRES_USER=admin \
  -e POSTGRES_DB=eru-service \
  -d postgres:17
```

## Update Maven Wrapper Version

```bash
# verify current version
$ ./mvnw --version

# update to latest version
$ ./mvnw -N wrapper:wrapper -Dmaven=3.10.1

# confirm update
$ ./mvnw --version
```
