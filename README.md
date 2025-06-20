# Library Application

A Spring Boot application for managing books. This application supports CRUD operations on books stored in MongoDB, with validation, mapping, and error handling.

## Getting Started

### Prerequisites

- Java 21 JDK
- Maven
- Docker & Docker Compose (for containerized setup)
- MongoDB running locally or via Docker

### Running Locally
1. Build the project:
```
mvn clean package
```

2. Run the application:
```
java -jar target/library*.jar
```

3. Access Swagger at: http://localhost:8080/swagger-ui/index.html

### Running with Docker Compose

1. Start the containers (mongodb and library application)
```
docker-compose up --build
```

2. Access the Swagger API: http://localhost:8080/swagger-ui/index.html

### Technologies Used

- **Java 21**
- **Spring Boot**
- **MongoDB**
- **Maven**
- **Docker & Docker Compose**
- **Spring Data MongoDB** — MongoDB integration and repository support
- **MapStruct** — DTO/entity conversions
- **Lombok** — Reduces boilerplate code (getters/setters/builders)
- **JUnit 5** & **Mockito** — Unit and integration testing
- **Testcontainers** — Containerized MongoDB for integration tests
- **Jackson** — JSON serialization/deserialization
- **Jakarta Validation (Bean Validation)** — Input validation annotations 

## Endpoints

- `GET /books` — List books with pagination (page and size query params)
- `GET /books/{id}` — Get a single book by ID 
- `POST /books` — Create a new book
- `PUT /books/{id}` — Update an existing book 
- `DELETE /books/{id}` — Delete a book by ID

## Assumptions & Design Considerations

- Duplicate ISBN should throw a 409 Conflict error, and should be 13 digits in length
- No defined Schema required
- The project is structured to support additional entity types (e.g., authors, users) in the future.
- The use of DTOs, mappers, service layer, and validation is intentional to reduce rewriting of code later.