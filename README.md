# Todo API

REST API for managing tasks, built with Spring Boot 3.

## Tech Stack

- Java 17
- Spring Boot 3.3.5
- Spring Data JPA
- H2 (dev) / PostgreSQL (prod)
- Flyway
- MapStruct
- Lombok
- Swagger UI
- Docker
-  docker-compose

## Requirements

- Java 17+
- Maven 3.8+
- Docker (optional)

## Getting Started

### 1. Clone the repository

```bash
  git clone https://github.com/danielStrielnikow/todo

cd todo
```

### 2. Create `.env` file

```bash
  cp .env.example .env
```

Default `.env` for local development (H2):

```properties
SPRING_PROFILES_ACTIVE=dev
DB_URL=jdbc:h2:mem:tododb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
DB_USERNAME=sa
DB_PASSWORD=
```

### 3. Run with Maven

```bash
  ./mvnw spring-boot:run
```

### 4. Run with Docker

```bash
  docker-compose up --build
```

The application will start on `http://localhost:8080`.

## API Documentation

Swagger UI is available at:

```
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```
http://localhost:8080/api-docs
```

## Endpoints

| Method | Endpoint                  | Description                        |
|--------|---------------------------|------------------------------------|
| GET    | /api/tasks                | Get all tasks (filter + paginate)  |
| POST   | /api/tasks                | Create a new task                  |
| GET    | /api/tasks/{id}           | Get task by ID                     |
| PUT    | /api/tasks/{id}           | Update task title and description  |
| PATCH  | /api/tasks/{id}/status    | Update task status                 |
| DELETE | /api/tasks/{id}           | Delete task                        |
| GET    | /api/tasks/stats          | Get task statistics                |

### Filtering & Pagination

```
GET /api/tasks?status=NEW&keyword=buy&page=0&size=10&sort=createdAt,desc
```

| Parameter | Description                              |
|-----------|------------------------------------------|
| status    | Filter by status: NEW, IN_PROGRESS, DONE |
| keyword   | Search by title (case-insensitive)       |
| page      | Page number (default: 0)                 |
| size      | Page size (default: 10)                  |
| sort      | Sort field and direction                 |

## Task Status Flow

```
NEW → IN_PROGRESS → DONE
```

Status can only be changed through `PATCH /api/tasks/{id}/status`. Skipping steps or going backwards is not allowed.

## Running Tests

```bash
  ./mvnw test
```

Tests include:
- Unit tests (`@ExtendWith(MockitoExtension.class)`)
- Controller tests (`@WebMvcTest`)
- Integration tests (`@SpringBootTest`)

## Running in Production (PostgreSQL)

### 1. Update `.env` file

```properties
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:postgresql://localhost:5432/tododb
DB_NAME=tododb
DB_USERNAME=your_username
DB_PASSWORD=your_password
```

### 2. Run PostgreSQL

Make sure PostgreSQL 18 is running and the database exists:

```sql
CREATE DATABASE tododb;
```

### 3. Start the application

```bash
  ./mvnw spring-boot:run
```

Or with Docker Compose (add PostgreSQL service to `docker-compose.yml`):

```yaml
services:
  todo-app:
    build: .
    ports:
      - "8080:8080"
    env_file:
      - .env
    depends_on:
      - postgres

  postgres:
    image: postgres:18
    env_file:
      - .env
    environment:
      POSTGRES_DB: ${DB_NAME}
      POSTGRES_USER: ${DB_USERNAME}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    ports:
      - "5432:5432"
```

Then run:

```bash
  docker-compose up --build
```

Flyway will automatically run all migrations on startup and create the required tables.

## Actuator (dev only)

| Endpoint                  | Description               |
|---------------------------|---------------------------|
| /actuator/health          | Application health status |
| /actuator/info            | Application info          |

Example health response:
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP", "details": { "database": "H2" } },
    "diskSpace": { "status": "UP" }
  }
}
```

## H2 Console (dev only)

```
http://localhost:8080/h2-console
```

JDBC URL: `jdbc:h2:mem:tododb`

## Possible Extensions

- **Spring Security** — JWT-based authentication and authorization
- **Soft delete** — mark tasks as deleted instead of removing from database
- **Due date** — add deadline field with overdue status detection
- **Caching** — cache task statistics with Spring Cache (`@Cacheable`)
- **Rate limiting** — protect API with Bucket4j or similar
