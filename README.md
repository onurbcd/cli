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

### Validate before pushing

`./validate.sh` runs locally what the GitHub Actions workflow runs, except Sonar (which runs on push). If it passes, the workflow's build and lint steps should pass too.

```bash
./validate.sh              # same steps as the workflow, from a clean build
./validate.sh --quick      # one strict pass, stops at the first failing tool; faster for iterating
./validate.sh --no-clean   # reuse the existing target/ directory
```

The default mode mirrors the workflow: `verify -Plint` in report-only mode first (Error Prone, unit tests, JaCoCo report and gate, Spotless check, then Checkstyle, PMD and SpotBugs, so every tool runs and every report exists), a check that the JaCoCo report exists, and finally the enforcement run that fails on any Checkstyle, PMD or SpotBugs finding. It prints a summary (tests, line coverage, and the number of findings per tool) and exits non-zero on the first failing step, so it can also be used from a git hook. It needs JDK 25 (`sdk env`). `--quick` runs `verify -Plint -Dlint.fail=true` once; it is faster but shows only the first failing linter, so use the default mode before pushing. A full run took about a minute on the author's machine.

### Import hygiene (Spotless)

The build checks the imports of every Java file under `src` with the Spotless Maven plugin: no unused imports, and a fixed order (all other imports, then `javax`, then `java`, then `static`, each group separated by a blank line; this is IntelliJ's default order). Only imports are checked; code formatting is not.

`./mvnw verify` runs `spotless:check` and fails the build when a file does not follow the rule. To check or fix on demand:

```bash
./mvnw spotless:check      # report files that violate the rule, without changing them
./mvnw spotless:apply      # fix imports in place
```

If you use IntelliJ, enabling "Reformat code" and "Optimize imports" under Settings > Tools > Actions on Save keeps files compliant, as long as the IDE's import order is the default one (Settings > Editor > Code Style > Java > Imports).

### Static analysis

Two layers, with different costs:

**Error Prone (always on).** It runs inside `javac`, so every `compile`, `test` and `verify` reports likely bugs as compiler warnings (`[WARNING] ... [EqualsGetClass] ...`). Generated code (Lombok, MapStruct, QueryDSL) is not reported. Findings are warnings; only Error Prone's error-level checks fail the compilation. The code base currently compiles with no warnings, so any new one is worth looking at.

Error Prone needs the `--add-exports`/`--add-opens` flags for `jdk.compiler` on JDK 17 and later. They live in `.mvn/jvm.config`, which `./mvnw` and IntelliJ's Maven import read automatically. If you run a plain `mvn` from another place, make sure that file is honored, or the compilation fails with `IllegalAccessError`.

**Checkstyle, PMD and SpotBugs (opt-in `lint` profile).** They slow `verify` down, so they only run when asked:

```bash
./mvnw verify -Plint                   # report only: prints findings, never fails the build
./mvnw verify -Plint -Dlint.fail=true  # fail the build on any finding
```

| Tool | Looks for | Configuration | Report |
|---|---|---|---|
| Checkstyle | Naming and convention slips (inner assignments, missing braces, long methods). Layout and imports are not checked. | `config/checkstyle/checkstyle.xml` | `target/checkstyle-result.xml` |
| PMD | Bug-prone code and best-practice violations (unused parameters, default charset, overridable calls in constructors). | `config/pmd/ruleset.xml` | `target/pmd.xml` |
| SpotBugs | Probable bugs in the bytecode (null dereferences, unnecessary boxing). Threshold: medium. | `config/spotbugs/exclude.xml` | `target/spotbugsXml.xml` |

Checkstyle and PMD analyze only hand-written code under `src/main/java`; tests and generated sources (QueryDSL `Q*`, MapStruct implementations) are skipped. SpotBugs works on the compiled classes and excludes the generated ones through its filter file.

Each configuration file lists what was excluded and why (for example `EI_EXPOSE_REP` in SpotBugs, caused by Lombok getters and Spring constructor injection). To silence a rule, change it in the file above with a comment explaining the reason; avoid `@SuppressWarnings` on individual classes. The profile is report-only by default so it can be adopted gradually: fix the findings, then enable `-Dlint.fail=true` (or set `lint.fail` to `true` in `pom.xml`) so new findings break the build.

The GitHub Actions workflow runs a plain `./mvnw verify`, so it includes Error Prone and the Spotless check but not the `lint` profile.

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

The value is one class-file pattern: slashes, `.class` suffix, wildcards allowed. If the class is below 100% the build fails and prints which rule was violated. Once a class reaches 100%, add its pattern as an `<include>` in the `jacoco-check` execution of `pom.xml` so it stays protected, and then tick it off in `TODO.md`. When a whole package is done, one pattern for the package is enough (for example `com/onurbcd/cli/dto/**/*.class`); from then on, any new class in it must be tested too or the build fails. Generated code (mapper implementations, QueryDSL `Q*` classes) and the application bootstrap are not part of the target.

### Mutation testing (PIT)

100% coverage proves the code ran, not that the tests would notice a bug. PIT changes the code in small ways (flips a condition, removes a call) and checks that some test fails; a change that no test notices is a "surviving mutant" and means an assertion is missing. It is slow, so it is not part of the normal build. Run it on one class:

```bash
./mvnw test-compile org.pitest:pitest-maven:mutationCoverage \
  -Dpit.target.classes=com.onurbcd.cli.util.NumberUtil \
  -Dpit.target.tests='com.onurbcd.cli.util.NumberUtilTest*'
```

`test-compile` is part of the command on purpose: the PIT goal does not compile, so without it PIT mutates against stale `target/test-classes` and reports survivors that no longer exist. The report is written to `target/pit-reports/index.html`. The build fails if fewer than 90% of mutants are killed (`pit.mutation.threshold`).

### Writing tests with the Claude Code skill

The project has a skill, `.claude/skills/spring-boot-testing`, that writes the tests for **one class or one whole package per run**. In Claude Code, run:

```
/spring-boot-testing NumberUtil
/spring-boot-testing com.onurbcd.cli.config
```

A package argument covers the classes directly in that package (add `.**` to include sub-packages, e.g. `com.onurbcd.cli.service.**`). The skill lists the classes, skips what is not testable or already gated, asks for confirmation when there are more than about 8, and then runs the full workflow (tests, JaCoCo, PIT, gate) class by class. Use it for packages with few classes or when you want to do more in one go.

The skill finds the class, reads `pom.xml` to match the project's versions, lists every branch, exception and collaborator call, writes `NumberUtilTest` (and an `*IT` when a real database is needed), measures coverage with JaCoCo until the class reaches 100%, runs PIT (always, never skipped), and reports what it did. The skill is not allowed to skip any of its steps. It never runs write git commands, so review and commit the result yourself. A typical session for one class:

1. `/spring-boot-testing <ClassName>`
2. `./mvnw verify -Djacoco.check.includes=<package/path>/<ClassName>.class` to confirm 100%
3. Run the PIT command above and fix any surviving mutant
4. Add the class (or, for a finished package, the package pattern) to the permanent gate in `pom.xml` and update `TODO.md`

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
