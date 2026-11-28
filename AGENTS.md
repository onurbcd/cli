# Repository Guidelines

## Project Structure & Module Organization

This is a Java CLI built with Spring Shell and Maven. The target Java version and dependency upgrade plan are tracked in [`TODO.md`](TODO.md); follow its phases in order unless the user reprioritizes them. Application code lives under `src/main/java/com/onurbcd/cli`, organized by responsibility: `command` defines shell commands, `service` contains business logic, `persistency` holds JPA entities and repositories, and `dto`, `param`, `mapper`, `validator`, and `config` support those layers. Runtime configuration is in `src/main/resources/application.yml`; Flyway schema changes belong in `src/main/resources/db/migration` and use the sequence `V<number>__description.sql`. Test code and fixtures belong in `src/test/java` and `src/test/resources` respectively.

## Build, Test, and Development Commands

- `./mvnw clean package` compiles and packages the application.
- `./mvnw test` runs the test suite (Spring Boot Test/JUnit dependencies are configured).
- `./mvnw spring-boot:run` starts the CLI in the current environment.
- `./mvnw verify` runs Maven verification, the Spotless import check and generates the JaCoCo report under `target/site/jacoco`.
- `./validate.sh` runs everything the GitHub Actions workflow runs except Sonar (use `--quick` for a faster single pass). Run it before handing work back.
- `./mvnw verify -Plint` also runs Checkstyle, PMD and SpotBugs in report-only mode (add `-Dlint.fail=true` to fail on findings). Error Prone always runs as part of compilation. See "Static analysis" in `README.md`.

The application uses PostgreSQL. For a local database, follow the Docker example in `README.md`; configure credentials and connection settings through the application configuration before running database-backed flows. Use `./mvnw` so builds use the checked-in Maven wrapper.

## Coding Style & Naming Conventions

Follow `.editorconfig`: UTF-8, LF endings, spaces, and four-space indentation for Java (two for XML and YAML). Use standard Java naming: `UpperCamelCase` types, `lowerCamelCase` methods and fields, and descriptive package names in lowercase. Keep command, service, repository, and mapper responsibilities in their corresponding packages. Preserve existing conventions for Lombok and MapStruct rather than adding boilerplate or alternate mapping patterns.

## Testing Guidelines

Add tests under `src/test/java` using the production package structure. Name test classes `*Test` and keep fixtures in `src/test/resources`. The initial test suite is a planned item in [`TODO.md`](TODO.md). Run `./mvnw test` for changes; use `./mvnw verify` when checking the packaged build and coverage report. Add focused tests for command behavior, service logic, and validation when changing those areas.

## Working through `TODO.md`

When the user refers to the TODO ("pega o todo", "próximo item"), follow this flow:

1. Always read all the steps written here and execute every one of them, without skipping any.
2. Take the first unchecked item of the active phase, one item at a time; do not start the next one on your own.
3. Analyze the current state of the code, present a short plan and ask only about decisions that are the user's to make; then execute.
4. Verify before closing: run `./validate.sh` (or at least `./mvnw test` when the change is small) and mark the item `[x]` in `TODO.md` only after it passes.
5. When a phase has no unchecked items left, update "Fase ativa" in `TODO.md`.
6. Do not run write git operations; leave changes in the working tree for the user to commit.
7. Finish by stating what was verified and what was not (for example, startup against a real database).
8. Generate the commit message for the work done, following the Conventional Commit style described in "Commit & Pull Request Guidelines", and present it for the user to commit (do not commit).

## Commit & Pull Request Guidelines

Recent commits use Conventional Commit-style prefixes, often scoped, such as `feat(bill): ...`, `refactor(package): ...`, and `build(maven): ...`. Follow that pattern with a concise imperative summary. Pull requests should explain the change and its impact, link related issues when applicable, note relevant test commands/results, and include CLI output or screenshots when they help reviewers understand user-visible behavior.

## Database & Configuration

Do not commit credentials or local secrets. Keep schema changes in a new Flyway migration rather than editing migrations that may already have been applied. Check `src/test/resources/application.properties` and `src/main/resources/application.yml` when modifying environment-specific settings.
