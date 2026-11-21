# Coverage and quality gates

Goal per run: 100% line and branch coverage on the target class, with assertions strong enough to kill mutants.

## Measuring one class

1. `./mvnw -q test -Dtest=<Name>Test` (fast loop).
2. `./mvnw verify` (runs `*IT` and writes the JaCoCo report, XML at `target/site/jacoco/jacoco.xml`, HTML at `target/site/jacoco/index.html`).
3. For the class, read from `jacoco.xml`: `<class name="com/acme/Foo">` → `<counter type="LINE" missed=... />`, `type="BRANCH"`. Missed lines: `<sourcefile>` → `<line nr=".." mi=".." mb=".."/>` where `mi>0` is a missed instruction and `mb>0` a missed branch. Add a case for each, re-run.
4. Done only when `LINE` and `BRANCH` `missed="0"` for the class (also for synthetic lambdas and inner classes of it).

Typical hidden branches: `&&`/`||` operands, `switch` defaults, `Optional.orElseThrow`, enhanced `for` over empty collection, try-with-resources close paths, `catch` blocks, ternaries inside string formatting, Lombok `@Builder`/`equals` (excluded when `lombok.addLombokGeneratedAnnotation=true`).

## Allowed exclusions (generated code only)

- Lombok: `lombok.config` → `lombok.addLombokGeneratedAnnotation = true` (JaCoCo ignores `@Generated`).
- MapStruct impls, QueryDSL `Q*` classes, annotation-processor output: exclude via JaCoCo `<excludes>` (`**/*MapperImpl.class`, `**/Q*.class`) only if they are not hand written; MapStruct impls are still worth a mapper test, exclude them from the *rule*, not from tests.
- Application bootstrap `main` class (`@SpringBootApplication`).
- Everything else must be covered. Never exclude to dodge a hard class.

## This repo's gate (already in `pom.xml`)

- `./mvnw verify -Djacoco.check.includes=com/onurbcd/cli/util/NumberUtil.class` fails the build unless that class has 100% line and branch coverage. The value is **one Ant-style class-file pattern** (slashes, `.class` suffix); a dotted name matches nothing. Default (`__none__`) gates nothing.
- When a class reaches 100%, add its pattern as a new `<include>` in the `jacoco-check` execution so it stays gated (once a whole package is done, use one package pattern such as `com/onurbcd/cli/dto/**/*.class` instead of one include per class; new classes in it must then be tested too), and tick it off in `TODO.md` (Fase 2).
- Unit (`*Test`, Surefire) and integration (`*IT`, Failsafe) coverage go to the same `target/jacoco.exec`, so the report and check see both.
- PIT (on demand): `./mvnw org.pitest:pitest-maven:mutationCoverage -Dpit.target.classes=<fqcn> -Dpit.target.tests='<fqcn>Test*'` (threshold `pit.mutation.threshold`, default 90). Verified working with Java 25 and Boot 4.0.9 on `BalanceCommand`. `withHistory` needs an extra plugin, so it is off.

## Maven setup for other projects (propose to the user; do not apply silently when `pom.xml` has uncommitted changes)

Surefire for unit tests, Failsafe for `*IT`, JaCoCo with a check rule at `verify`:

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-failsafe-plugin</artifactId>
  <executions><execution><goals><goal>integration-test</goal><goal>verify</goal></goals></execution></executions>
</plugin>

<plugin>
  <groupId>org.jacoco</groupId>
  <artifactId>jacoco-maven-plugin</artifactId>
  <executions>
    <execution><id>prepare-agent</id><goals><goal>prepare-agent</goal></goals></execution>
    <execution><id>prepare-agent-it</id><goals><goal>prepare-agent-integration</goal></goals></execution>
    <execution><id>report</id><phase>verify</phase><goals><goal>report</goal></goals></execution>
    <execution>
      <id>check</id><phase>verify</phase><goals><goal>check</goal></goals>
      <configuration><rules><rule>
        <element>BUNDLE</element>   <!-- use CLASS while migrating class by class -->
        <limits>
          <limit><counter>LINE</counter><value>COVEREDRATIO</value><minimum>1.00</minimum></limit>
          <limit><counter>BRANCH</counter><value>COVEREDRATIO</value><minimum>1.00</minimum></limit>
        </limits>
      </rule></rules></configuration>
    </execution>
  </executions>
</plugin>
```

Because the project is covered one class at a time, use `<element>CLASS</element>` with `<includes>` listing the classes already done (or raise the bundle threshold progressively) so the build does not fail on untested classes. Keep versions managed or explicit as the project already does; check JaCoCo supports the project's Java release (JaCoCo 0.8.13+ for Java 24/25 class files).

## Mutation testing (PIT)

Coverage shows code ran, not that it was checked. PIT is mandatory for every class (workflow step 8, never skipped). Use it on the target class:

```xml
<plugin>
  <groupId>org.pitest</groupId>
  <artifactId>pitest-maven</artifactId>
  <dependencies>
    <dependency><groupId>org.pitest</groupId><artifactId>pitest-junit5-plugin</artifactId></dependency>
  </dependencies>
  <configuration>
    <targetClasses><param>com.acme.service.BillService</param></targetClasses>
    <targetTests><param>com.acme.service.BillServiceTest*</param></targetTests>
    <mutationThreshold>90</mutationThreshold>
    <withHistory>true</withHistory>
  </configuration>
</plugin>
```

Run: `./mvnw org.pitest:pitest-maven:mutationCoverage -Dpit.target.classes=<fqcn> -Dpit.target.tests='<fqcn>Test*'`. Report: `target/pit-reports/`. Each SURVIVED mutant is a missing assertion (return value not checked, boundary not tested, call not verified). Fix by asserting more, then re-run. Keep PIT out of the default build (slow); run it per class or in a CI profile. Confirm PIT and its JUnit plugin versions support the project's JUnit major and Java release before adding.

## Final report template

```
Class: <FQCN>
Tests: <n> in <file(s)>
Cases: <matrix summary or link to table>
Coverage (JaCoCo): LINE x/y, BRANCH x/y   (command + date)
PIT: killed a / total, survivors: <none | list>
Not covered / caveats: <none | why>
Production issues spotted: <none | list>
```
