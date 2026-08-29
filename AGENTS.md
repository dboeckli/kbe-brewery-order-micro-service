# AGENTS.md

Spring Boot 4 (parent 4.1.1) / Spring Framework 6 order microservice on **Java 25** (enforced by the
maven-enforcer plugin). Single Maven module, package `ch.dboeckli.springframeworkguru.kbe.order.services`.
It is a REST API (Spring Web MVC) for beer orders, backed by MySQL (JPA) and Artemis (JMS), and deployed
together with the sibling services beer (`kbe-brewery-beer-micro-service`), inventory
(`kbe-brewery-inventory-micro-service`) and inventory-failover (`kbe-brewery-inventory-failover`).

## Build & test commands

- Full build: `./mvnw clean verify` — format checks, unit (`*Test`, surefire) + IT (`*IT`, failsafe)
  tests, Helm lint/template. `./mvnw verify` also runs the unit tests.
- Unit tests only: `./mvnw test`. Single test: `./mvnw test -Dtest=BreweryOrderServiceIT#methodName`.
- `./mvnw clean install` additionally builds the Docker image and packages the Helm chart into
  `target/helm/repo/` (parent `*-chart-<version>.tgz` plus the `-jms-chart`/`-mysql-chart`
  subchart tgz). Skip the Docker build with `-Dskip.docker.build=true`.
- `-Dskip.start.stop.springboot=true` skips the in-build app boot (spring-boot:start/stop) that
  runs during the IT phase.
- Start locally: `./mvnw spring-boot:run` (app on `:8081`).

After changing code, always verify: run the relevant Maven goal above and report its output
(evidence, not just "done").

## Sandbox build quirk (background)

This sandbox mounts the repo via filesystem passthrough, which blocks symlinks — Spotless's
`npm install` (prettier) would fail with `EPERM` unless npm skips bin links. The sandbox kit sets
`npm_config_bin_links=false` globally (`spec.yaml` → `environment.variables`), so no manual export
is needed here. On a normal host (Windows/CI) this does not apply either.

## Formatting is enforced (fails the `validate` phase)

- Java: Spring Java Format → fix with `./mvnw spring-javaformat:apply`.
- Everything else (pom.xml, `**/*.md` except `AGENTS.md`/`CLAUDE.md`, json, application yaml,
  `**/*.sh`): Spotless → fix with `./mvnw spotless:apply`.

## External dependency gotcha

- DTOs and helpers come from the external module `ch.dboeckli...:kbe-brewery-lib` (GitHub Packages,
  `maven.pkg.github.com`). Without a PAT in `~/.m2/settings.xml` (server id `github`) the build
  cannot resolve dependencies.

## Test conventions

- Naming matters: `*Test` = unit (surefire), `*IT` = integration (failsafe). A `*Test` class will
  not run during `verify`'s failsafe phase and vice versa.
- ITs are `@SpringBootTest`; the build boots the app (spring-boot:start/stop) against the Docker
  Compose services defined in `compose.yaml` (mysql, jms) unless `-Dskip.start.stop.springboot=true`.

## Architecture

- Layered flow: `web/controllers` → `services` → `repositories` → `domain` entities.
- Mappers are MapStruct with the Lombok binding (`-Amapstruct.defaultComponentModel=spring`).
- Order validation is asynchronous via JMS (`validate-order` / `validate-order-result` queues,
  Artemis); inventory allocation goes through `allocate-order` / `allocate-order-result` /
  `allocation-failure`, deallocation via `deallocate-order`.
- The sibling services (beer/inventory/inventory-failover) are pulled in as Helm chart dependencies
  (Docker Hub `oci://registry-1.docker.io/domboeckli`) and via `compose.yaml` for local runs.

## Running locally

- `compose.yaml` is auto-started via `spring.docker.compose` on boot (mysql :3306, jms :61616/:8161).
- Artemis console: http://localhost:8161/console
- Manual API testing: IntelliJ HTTP files in `restRequest/`.
