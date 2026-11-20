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
