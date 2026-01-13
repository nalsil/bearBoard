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

## Performance Testing

### k6 Load & Stress Testing
The `k6/` directory contains comprehensive performance testing scripts for public endpoints:

```bash
# Quick start - basic functionality check
cd k6
./run-tests.sh smoke

# Load testing - normal traffic simulation
./run-tests.sh load

# See k6/QUICK_START.md for detailed instructions
```

**Available Tests**:
- **Smoke Test**: Basic functionality verification (1 VU, 1 min)
- **Load Test**: Normal load performance testing (10-50 VU, 10 min)
- **Stress Test**: System limits and recovery testing (50-200 VU, 14 min)
- **Spike Test**: Sudden traffic spike handling (10-500 VU, 7 min)

**Documentation**:
- Quick Start: `k6/QUICK_START.md`
- Full Guide: `k6/README.md`
- Configuration: `k6/config.js`

**Prerequisites**:
- k6 installed (`brew install k6` or `choco install k6`)
- Application running on port 8080
- Test data in database (company, products, boards, posts)
