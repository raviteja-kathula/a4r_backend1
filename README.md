# Product API

Spring Boot REST API for the Commerce4Retail product catalogue, implementing the Product Specification defined in Jira story [COM-66](https://deloitte-digital-training.atlassian.net/browse/COM-66).

## Tech Stack

| Layer | Technology |
|---|---|
| Runtime | Java 21 |
| Framework | Spring Boot 3.2.3 |
| Persistence | Spring Data JPA + H2 (in-memory) |
| Validation | Jakarta Bean Validation |
| Documentation | SpringDoc OpenAPI 3 (Swagger UI) |
| Testing | JUnit 5 + Mockito + Spring Boot Test |
| Coverage | JaCoCo ≥ 80% line coverage |

## Quick Start

### Prerequisites

- Java 21+
- Maven 3.8+

### Run

```bash
JAVA_HOME=/path/to/java21 mvn spring-boot:run
```

Server starts on **http://localhost:8080**

### Swagger UI

```
http://localhost:8080/swagger-ui.html
```

### H2 Console

```
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:productdb
Username: sa  Password: (empty)
```

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

## API Endpoints

### Products

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/products` | List products (filterable/sortable/paginated) |
| GET | `/api/v1/products/{id}` | Get product by ID |
| GET | `/api/v1/products/slug/{slug}` | Get product by slug |
| POST | `/api/v1/products` | Create product |
| PUT | `/api/v1/products/{id}` | Update product |
| DELETE | `/api/v1/products/{id}` | Soft-delete product |
| GET | `/api/v1/products/{id}/reviews` | Get product reviews |
| POST | `/api/v1/products/{id}/reviews` | Add review |

### Brands

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/brands` | List brands |
| GET | `/api/v1/brands/{id}` | Get brand by ID |
| POST | `/api/v1/brands` | Create brand |
| PUT | `/api/v1/brands/{id}` | Update brand |
| DELETE | `/api/v1/brands/{id}` | Delete brand |

### Categories

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/categories` | List categories |
| GET | `/api/v1/categories/roots` | List root categories |
| GET | `/api/v1/categories/{id}` | Get category by ID |
| GET | `/api/v1/categories/slug/{slug}` | Get category by slug |
| POST | `/api/v1/categories` | Create category |
| PUT | `/api/v1/categories/{id}` | Update category |
| DELETE | `/api/v1/categories/{id}` | Delete category |

## Running Tests

```bash
JAVA_HOME=/path/to/java21 mvn clean verify
```

Tests: 69 | Coverage Gate: ≥ 80% LINE

## Environment Variables

All config via `src/main/resources/application.yml`. No secrets in code.

## References

- Jira Story: COM-66
- IPP Workflow: `ipp_workflow_specs_api_first.md`
- Data Model: `_specs/data_modelling.md`
