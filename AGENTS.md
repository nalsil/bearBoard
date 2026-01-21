# AGENTS.md

This file provides guidance to agentic coding assistants working with the Bear Spring Boot application.

## Build, Lint, and Test Commands

```bash
# Build
./gradlew build

# Run application
./gradlew bootRun

# Clean and rebuild
./gradlew clean build

# Run all tests
./gradlew test

# Run tests with detailed output
./gradlew test --info

# Run a specific test class (use double backslashes on Windows, single on Unix)
./gradlew test --tests "com.nalsil.bear.service.ProductServiceTest"

# Run a specific test method
./gradlew test --tests "com.nalsil.bear.service.ProductServiceTest.testGetVisibleProductsByCompanyId_Success"

# Run tests in watch mode
./gradlew test --continuous

# Code coverage
./gradlew jacocoTestReport
./gradlew jacocoTestCoverageVerification

# Check dependencies
./gradlew dependencies

# List all tasks
./gradlew tasks
```

## Code Style Guidelines

### Imports
- Organize by groups: standard library, third-party, project imports
- Prefer explicit imports over wildcard imports
- Common imports: `reactor.core.publisher.Mono`, `reactor.core.publisher.Flux`
- MapStruct: `org.mapstruct.*`
- Lombok: `lombok.*`

### Formatting
- Use **tabs** for indentation (not spaces)
- 4 indentation levels per tab
- One blank line between methods and logical sections
- Maximum line length: not strictly enforced, but prefer readability

### Types
- Always use reactive types: `Mono<T>` for single results, `Flux<T>` for multiple
- Never use blocking operations in reactive chains (e.g., `block()`, `blockFirst()`)
- Use `ServerWebExchange` instead of `HttpServletRequest` in WebFlux
- Database access: Use R2DBC, not JDBC/JPA

### Naming Conventions
- Classes: PascalCase (`ProductController`, `ProductService`)
- Methods: camelCase (`getProductById`, `createProduct`)
- Variables: camelCase (`companyId`, `productFlux`)
- Constants: UPPER_SNAKE_CASE
- Packages: lowercase with dots (`com.nalsil.bear.domain.product`)
- DTOs: `[Action][Entity]Request` (e.g., `CreatePostRequest`), `[Entity]Response`

### Annotations
- Always include `@Slf4j` for logging
- Use Lombok: `@Data` for entities/DTOs, `@RequiredArgsConstructor` for constructors
- Controllers: `@Controller`, `@RequestMapping`, or `@RestController`
- Services: `@Service`, `@RequiredArgsConstructor`
- Repositories: `@Repository`
- Configurations: `@Configuration`, `@EnableWebFluxSecurity`
- MapStruct: `@Mapper(componentModel = "spring")`

### Error Handling
- Return `Mono.error()` for reactive error propagation
- Use `switchIfEmpty()` or `defaultIfEmpty()` for empty flows
- Use custom exceptions in `com.nalsil.bear.exception` package
- Global exception handling via `GlobalExceptionHandler`

### Testing
- Use JUnit 5 with `@ExtendWith(MockitoExtension.class)`
- Mock dependencies with `@Mock` and inject with `@InjectMocks`
- Verify reactive streams with `StepVerifier` from `reactor.test`
- Use `@BeforeEach` for test data setup
- Test methods should use `@DisplayName("Description in Korean")`

### Controllers
- Return `Mono<Rendering>` for Thymeleaf templates
- Return `Mono<ResponseEntity<T>>` for API responses
- Use Swagger annotations: `@Tag`, `@Operation`, `@ApiResponses`, `@ApiResponse`
- Always log access with `log.info()` or `log.debug()`
- Handle tenant context with `.contextWrite(ctx -> TenantContextHolder.setCurrentTenant(ctx, companyCode))`

### Services
- Always reactive: `Mono<T>` or `Flux<T>`
- Use `.doOnSuccess()`, `.doOnError()`, `.doOnComplete()` for side-effect logging
- Never block the reactive chain

### Entities
- Use `@Table("table_name")` for explicit table mapping
- Use `@Column("column_name")` for snake_case DB columns
- Include `@Id` for primary keys
- Use Lombok's `@Builder` for flexible object creation
- Use `LocalDateTime` for timestamps

### Repositories
- Extend `R2dbcRepository<Entity, ID>`
- Use Spring Data derived query methods when possible
- Custom queries with `@Query` for complex operations
- Always return `Mono<T>` or `Flux<T>`

### Configuration
- Use `@Configuration` classes for Spring configuration
- Security: Use `SecurityWebFilterChain` with `.authorizeExchange()`
- CORS: Configure in `WebFluxConfig`, disable in SecurityConfig
- Use `@Bean` for component registration

### Mappers
- Use MapStruct for entity-DTO conversion
- Interface-based mappers with `@Mapper(componentModel = "spring")`
- Use `@Mapping` for custom field mappings
- Use `@MappingTarget` for update operations

### Security
- JWT-based authentication via `JwtAuthenticationFilter`
- Multi-tenancy: tenant ID passed via JWT
- Paths: `/admin/**` requires authentication, `/actuator/health` and `/swagger-ui/**` are public

### Database
- Use R2DBC PostgreSQL
- Connection pooling configured in `application.yaml`
- Use reactive types in repository methods
- No JPA/Hibernate entities

### Logging
- Use SLF4J via `@Slf4j`
- Levels: `log.debug()` for detailed flow, `log.info()` for business events, `log.warn()` for issues, `log.error()` for exceptions
- Log reactive operations with `.doOnNext()`, `.doOnComplete()`, `.doOnError()`

### File Structure
```
src/main/java/com/nalsil/bear/
  - BearApplication.java
  - controller/
    - admin/
    - public_/
  - service/
  - domain/[entity]/
    - [Entity].java
    - [Entity]Repository.java
  - dto/
    - request/
    - response/
  - mapper/
  - config/
  - filter/
  - exception/
  - util/
```

## Coverage Requirements
- Minimum test coverage: 70% (enforced by JaCoCo)
- Always run `./gradlew test` before committing
- Verify coverage with `./gradlew jacocoTestCoverageVerification`
