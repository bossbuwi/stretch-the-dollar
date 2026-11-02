# Copilot instructions

## Build and tests

- The project uses Java 21 and the Maven wrapper. Run the test suite with `./mvnw test`.
- Run one test class with `./mvnw -Dtest=UserServiceTest test`, or one test method with `./mvnw -Dtest=UserServiceTest#shouldReturnAllUsers test`.
- Build the application with `./mvnw package`. The Dockerfile builds with `./mvnw package -DskipTests`.
- No dedicated lint command is configured in `pom.xml`.

## Architecture

- This is a Spring Boot REST API. Controllers expose endpoints and delegate to service interfaces; implementations in `service/impl` hold application logic and use Spring Data repositories for persistence.
- API request and response DTOs live in `api/request` and `api/response`. MapStruct mappers convert between DTOs, entities, and response projections; mapper implementations are generated during compilation.
- JPA entities map to the relational model. Liquibase owns schema changes: add migrations under `src/main/resources/db/changes` and include them in `db/changelog-master.xml`. Hibernate schema generation is disabled.
- Transactions are PostgreSQL-backed in the default configuration. Tests use an in-memory H2 database. `application-local.properties` is a local profile override.
- Transaction read responses come from the immutable `transaction_details_view` database view, mapped as `entity/view/TransactionDetails`; update both the Liquibase view migration and Java projection when its shape changes.
- Authentication is stateless JWT: `SecurityConfig` installs `JwtAuthenticationFilter`, while `AuthService` handles registration/login and `JwtService` handles token operations. `GlobalExceptionHandler` delegates errors to the error response factory and exception handlers.

## Repository conventions

- Keep controller, service interface, and service implementation responsibilities separate. Use constructor injection and return API DTOs rather than exposing JPA entities from endpoints.
- Put request validation constraints on request DTO fields and use `@Valid` at controller request-body boundaries.
- Authorization is partly implemented with AOP annotations (`@WithPermission` and `@CheckOwnership`), in addition to the HTTP rules in `SecurityConfig`. When changing a protected endpoint or service method, preserve or update the relevant annotations and their aspect tests.
- Use MapStruct interfaces with `componentModel = "spring"` for application mappings; define explicit field mappings and named conversion methods when property names or representations differ.
- Keep database schema and view definitions in Liquibase rather than relying on Hibernate DDL. Keep JPA table/column mappings and the Java view projection aligned with those definitions.
- Tests use JUnit 5. Service unit tests commonly use Mockito (`@Mock`, `@InjectMocks`) and a real MapStruct mapper via `Mappers.getMapper`; nested test classes group cases by method, and `helper` contains shared test data builders.
