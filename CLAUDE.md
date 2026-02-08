# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Frida Calculs API** - A Spring Boot REST API for calculating Islamic inheritance shares (Faraid) according to Islamic law based on Quran verses (Surah An-Nisa 4:11-12, 176).

- **Language**: Java 21
- **Framework**: Spring Boot 3.5.0
- **Build Tool**: Maven 3.9+
- **API Version**: 1.0.0
- **Port**: 8080

## Common Commands

### Building and Running

```bash
# Clean and compile (skip tests)
mvn clean package -DskipTests

# Clean and compile (with tests)
mvn clean package

# Run the application
mvn spring-boot:run

# Run tests only
mvn test

# Run a single test class
mvn test -Dtest=CalculPartsServiceTest

# Run a specific test method
mvn test -Dtest=CalculPartsServiceTest#testMethodName
```

### Docker

```bash
# Build Docker image
docker build -t frida-calculs-api:1.0.0 .

# Run container
docker run -d -p 8080:8080 --name frida-api frida-calculs-api:1.0.0

# View logs
docker logs -f frida-api
```

### Testing the API

```bash
# Health check
curl http://localhost:8080/api/v1/heritage/status

# Calculate inheritance shares
curl -X POST http://localhost:8080/api/v1/heritage/calculate \
  -H "Content-Type: application/json" \
  -d '{"sexeDefunt":"M","conjointVivant":true,"nbFilles":1,"nbGarcons":1}'
```

### API Documentation

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs
- **Actuator Health**: http://localhost:8080/actuator/health

## Architecture and Code Structure

### High-Level Architecture

This is a **layered architecture** Spring Boot REST API:

1. **Controller Layer** (`CalculsPartsController`) - REST endpoints, validation, response mapping
2. **Service Layer** (`CalculPartsService`) - Business logic for Islamic inheritance calculation
3. **Validator Layer** (`FamilyRequestValidator`) - Business rule validation
4. **Model Layer** - DTOs, domain models, and mathematical utilities
5. **Exception Layer** - Centralized error handling

### Core Calculation Flow

The inheritance calculation follows Islamic law (Faraid) in a **sequential reduction pattern**:

1. **Start with full inheritance** (Fraction 1/1)
2. **Subtract fixed shares** in order:
   - Spouse (1/8 or 1/4 for male deceased, 1/4 or 1/2 for female)
   - Father (1/6 if children exist, 2/3 otherwise)
   - Mother (1/6 if children exist, 1/3 or 1/6 otherwise)
3. **Distribute remaining** to:
   - Children (girls get 1 part, boys get 2 parts - "two shares for a male")
   - Siblings (only if remaining fraction exists)
4. **Reduce all fractions** to common denominator
5. **Track remainder** (part restant)

This sequential approach is implemented in `CalculPartsService.calculParts()` starting at line 22.

### Critical Business Logic - Fraction Class

**`model/Fraction.java`** is the mathematical foundation:
- Handles fraction arithmetic (add, subtract, multiply, divide)
- **Auto-reduces** fractions on creation via GCD calculation
- **`reduireAuMemDenominateur()`** (line 98) - Converts list of fractions to common denominator using LCM
- All inheritance calculations rely on this class

**Key Detail**: The `Heritiers` class (line 64-180) contains **static factory methods** for each heir type (conjoint, pere, mere, fille, garcon, soeur, frere) that encode the Islamic inheritance rules directly.

### Validation Architecture

Two-level validation system:

1. **Bean Validation** (JSR-380) - in `FamilyRequest.java`:
   - `@NotNull`, `@Min`, `@Max` annotations
   - Validates syntax and ranges

2. **Business Validation** - in `FamilyRequestValidator.java`:
   - `validate()` - Checks family composition logic (at least one heir, reasonable totals)
   - `validateIslamicRules()` - Enforces Islamic inheritance rules (e.g., siblings can't inherit if father exists)

Both are called in sequence in the controller (lines 76-77 of `CalculsPartsController.java`).

### Exception Handling

**Centralized** via `GlobalExceptionHandler` using `@ControllerAdvice`:
- Returns **RFC 7807 Problem Details** format (ErrorResponse model)
- Maps validation errors with field-level detail
- Logs all exceptions with context

### Response Format

The API uses **enriched responses** via `HeritageResponse`:
- Includes calculation metadata (ID, timestamp, summary)
- Contains list of heirs with their fractions
- Provides common denominator for easy percentage calculation
- Includes flags like `calculComplet` to indicate if all inheritance was distributed

Built using factory method: `HeritageResponse.fromCalculation()` (see `model/HeritageResponse.java`)

## Important Implementation Details

### Deprecated Endpoint

- **`GET /api/v1/heritage/calculs`** (line 37-46 in controller) - Old endpoint with query params, still exists for backward compatibility
- **`POST /api/v1/heritage/calculs`** (line 104-118) - Marked deprecated in Swagger
- **Use instead**: `POST /api/v1/heritage/calculate` (modern endpoint with full validation)

### Logging Strategy

- Uses **Lombok `@Slf4j`** throughout
- Service layer logs calculation steps (each heir calculation + remaining fraction)
- Controller logs request entry and completion with summary
- Validator logs warnings for edge cases (e.g., children + siblings present)

Configuration in `application.properties`:
- Root level: INFO
- Package `com.med.frida_calculs_app`: DEBUG
- Spring Web: INFO

### CORS Configuration

Two approaches used (choose based on need):
1. **Global** - `config/CorsConfig.java` - WebMvcConfigurer
2. **Per-endpoint** - `@CrossOrigin` annotation on controller methods

Allowed origins configured in `application.properties` (line 16).

### Testing Structure

- **`CalculPartsServiceTest`** - Unit tests for business logic (6 tests)
- **`CalculsPartsControllerIntegrationTest`** - Integration tests with MockMvc (8 tests)
- Tests cover: valid cases, validation errors, edge cases, Islamic rule violations

## Code Patterns to Follow

### When Adding New Heir Types

1. Add enum value to `TypeHeritier` if needed
2. Add field to `Heritiers` class
3. Create static factory method in `Heritiers` following Islamic rules
4. Add calculation step in `CalculPartsService.calculParts()` in correct sequence
5. Add fraction to `fractionsList` for common denominator reduction
6. Update tests with new scenarios

### When Adding New Validations

1. Add annotation to `FamilyRequest` for syntax validation
2. Add business logic to `FamilyRequestValidator.validate()` or `validateIslamicRules()`
3. Ensure errors are descriptive (user-facing messages)
4. Add test cases for validation failures

### When Modifying Fraction Calculations

- **Always test** with multiple scenarios
- Verify common denominator reduction still works
- Check that fractionRestant is tracked correctly through the chain
- Islamic inheritance should always sum to ≤ 1 (remainder goes to "part restant")

## Configuration Files

### application.properties

Key settings:
- Port: 8080
- Logging: DEBUG for app package
- Swagger: enabled at `/swagger-ui.html`
- Actuator: health, info, metrics exposed
- Jackson: pretty-print JSON, exclude nulls
- CORS: localhost:4200, 3000, 8080

### pom.xml

Key dependencies:
- `spring-boot-starter-web` - REST API
- `spring-boot-starter-validation` - Bean Validation
- `springdoc-openapi-starter-webmvc-ui` (2.8.13) - Swagger
- `spring-boot-starter-actuator` - Monitoring
- `lombok` - Reduce boilerplate

## Islamic Law Context

The calculation implements **Sunni Islamic inheritance law (Faraid)**:
- Fixed shares (Fard) for parents, spouse based on Quran
- Residuary (Asabah) for children and siblings
- Male heir receives twice the share of female heir (2:1 ratio)
- Complex precedence rules (parents block siblings, children affect spouse share, etc.)

**Important**: The business logic in `Heritiers` class static methods directly encodes these religious rules. Changes should be validated against Islamic jurisprudence sources.

## Project History

Recent transformations (see AMELIORATIONS.md, TRANSFORMATION_COMPLETE.md):
- Converted from basic calculator to production REST API
- Added comprehensive validation (bean + business)
- Implemented standardized error handling (RFC 7807)
- Added OpenAPI documentation
- Created integration test suite
- Dockerized application
