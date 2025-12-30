# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Bear** is a Spring Boot 3.5.9 application built with Java 21, using reactive programming (WebFlux), Spring Security, Thymeleaf templating, and PostgreSQL database.

### Technology Stack
- **Framework**: Spring Boot 3.5.9
- **Java Version**: 21
- **Build Tool**: Gradle
- **Key Dependencies**:
  - Spring WebFlux (reactive web)
  - Spring Security
  - Thymeleaf + Spring Security integration
  - PostgreSQL (runtime)
  - Spring Boot Actuator
  - Lombok
  - Spring Boot DevTools

## Development Commands

### Build and Run
```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun

# Clean and rebuild
./gradlew clean build
```

### Testing
```bash
# Run all tests
./gradlew test

# Run tests with detailed output
./gradlew test --info

# Run a specific test class
./gradlew test --tests "com.nalsil.bear.BearApplicationTests"

# Run tests continuously (watch mode)
./gradlew test --continuous
```

### Development
```bash
# Check dependencies
./gradlew dependencies

# List all tasks
./gradlew tasks

# Generate application JAR
./gradlew bootJar
```

## Architecture Notes

### Reactive Stack
This application uses Spring WebFlux for reactive programming. All web endpoints should return reactive types (`Mono<T>`, `Flux<T>`) rather than blocking types. When implementing new features:
- Use `WebClient` instead of `RestTemplate`
- Database access should use R2DBC instead of JPA/JDBC
- Controllers should return `Mono<T>` or `Flux<T>`

### Security Configuration
The application includes Spring Security with Thymeleaf integration. Security configurations should:
- Be placed in dedicated `@Configuration` classes
- Use WebFlux security patterns (`.securityWebFilterChain()`)
- Integrate with Thymeleaf via `thymeleaf-extras-springsecurity6`

### Database
PostgreSQL is configured as the runtime database. Connection properties should be defined in `application.yaml`. For reactive access, ensure R2DBC dependencies are added if not already present.

### Project Structure
```
src/main/java/com/nalsil/bear/
  - BearApplication.java (main entry point)
  - [Additional packages for controllers, services, repositories, config]
src/main/resources/
  - application.yaml (Spring configuration)
  - [templates/, static/ for Thymeleaf and static resources]
```

### Actuator Endpoints
Spring Boot Actuator is enabled for monitoring and management. Endpoints are available at `/actuator/*` and should be properly secured in production.
