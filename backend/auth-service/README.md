Auth Service — Developer Guide

Purpose
- Authentication and authorization microservice for the Vaccination App.
- Provides JWT-based login/refresh, RBAC, user and permission management, and auditing.

Tech stack
- Java 21, Maven
- Spring Boot 3.5 (Web, Security, Validation, Actuator)
- JPA/Hibernate + PostgreSQL
- Flyway database migrations
- Testcontainers (integration testing)
- Lombok, MapStruct
- OpenAPI/Swagger UI (springdoc)

Project layout (key paths)
- src/main/java/com/niyiment/authservice
  - config — Spring config (security, Swagger, CORS, beans)
  - controller — REST controllers
  - domain
    - dto — request/response DTOs
    - entity — JPA entities
    - mapper — MapStruct mappers
  - exceptions — Global exception handling
  - repository — Spring Data JPA repositories
  - services — Business/services layer
    - security — JWT, user details, filters
- src/main/resources
  - application.yaml — app configuration
  - db/migration — Flyway SQL migrations (V__*.sql)
- src/test/java/com/niyiment/authservice — unit/integration tests
- compose.yaml — Dev PostgreSQL via Docker Compose
- pom.xml — dependencies and build

Setup
1) Prerequisites: Java 21, Maven 3.14+, Docker (for Testcontainers/Compose).
2) Environment:
   - .env (optional): database creds loaded by Compose.
   - application.yaml: local overrides via env vars (SPRING_DATASOURCE_*, JWT_*).
3) Database: use Testcontainers (tests) or docker compose for local dev.

Run the app
- Local (no Docker DB): ensure your PostgreSQL is running and configured in application.yaml/env, then:
  - mvn spring-boot:run
- With Docker Compose DB:
  - docker compose up -d postgres
  - mvn spring-boot:run
- Packaged jar:
  - mvn -DskipTests package && java -jar target/auth-service-0.0.1-SNAPSHOT.jar

API docs and health
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- Actuator: /actuator/health, /actuator/info

Running tests
- Unit + integration: mvn test
  - Uses Testcontainers (pulls postgres:latest automatically).
- Single test: mvn -Dtest=ClassName#methodName test

Code organization guidelines
- Controllers: HTTP-only concerns (validation, status codes). Use DTOs, never expose entities.
- Services: business logic, transactional boundaries.
- Repositories: data access via Spring Data JPA.
- Security: keep JWT, filters, and method security under services/security and config.
- Mapping: centralize entity↔DTO mapping with MapStruct in domain/mapper.
- Migrations: all schema/data changes via Flyway SQL in src/main/resources/db/migration.
- Validation: javax/jakarta validation on DTOs; handle errors in GlobalExceptionHandler.

Best practices
- Package-by-feature (as above) and keep classes small and focused.
- Prefer constructor injection; avoid field injection.
- Add tests for services and critical controllers; use @DataJpaTest for repos when useful.
- Log meaningful messages (no secrets); prefer SLF4J parameterized logs.
- Secure endpoints via method-level annotations and config rules; keep public endpoints explicit.
- Keep environment configs out of source control; use env vars for secrets.

Common commands
- Format/compile: mvn -DskipTests verify
- Run app: mvn spring-boot:run
- Run tests: mvn test
- Start DB (Compose): docker compose up -d postgres
- Apply migrations (on startup by Flyway): configured via application.yaml

Troubleshooting
- Port in use: change server.port in application.yaml or free 8080.
- Testcontainers failing: ensure Docker daemon is running and network access present.
- DB connection errors: verify SPRING_DATASOURCE_URL, user, password, and that the postgres container is healthy.
