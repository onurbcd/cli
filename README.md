# Eru Service

[![Java Maven Sonar CI](https://github.com/onurbcd/eruservice/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/onurbcd/eruservice/actions/workflows/build.yml)

## Postgres in Docker

```
docker run -p5432:5432 --name postgres -e POSTGRES_PASSWORD=secret -e POSTGRES_USER=admin -e POSTGRES_DB=eru-service -d postgres:15
```

## Update Maven Wrapper Version

```shell
# verify current version
$ ./mvnw --version

# update to latest version
$ ./mvnw -N wrapper:wrapper -Dmaven=3.9.11

# confirm update
$ ./mvnw --version
```
