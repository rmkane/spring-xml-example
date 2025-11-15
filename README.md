<!-- omit in toc -->
# Spring XML Example

A Spring Boot application demonstrating a layered architecture with XML request handling and JSON response formatting, using DTOs, entities, and MapStruct for type-safe mapping. Features nested data structures (info sections) and simplified entry objects with enum types.

<!-- omit in toc -->
## Table of Contents

- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Quick Start](#quick-start)
  - [Local Development](#local-development)
    - [Using Make (Recommended)](#using-make-recommended)
    - [Manual Commands](#manual-commands)
  - [Running Tests](#running-tests)
    - [Unit Tests Only](#unit-tests-only)
    - [Integration Tests Only](#integration-tests-only)
    - [All Tests](#all-tests)
    - [Test Database](#test-database)
- [Architecture Overview](#architecture-overview)
- [Data Flow](#data-flow)
  - [Create Calendar Flow](#create-calendar-flow)
  - [Retrieve Calendar Flow](#retrieve-calendar-flow)
  - [Delete Calendar Flow](#delete-calendar-flow)
- [Data Models](#data-models)
  - [CalendarRequest](#calendarrequest)
  - [Calendar](#calendar)
  - [CalendarResponse](#calendarresponse)
  - [CalendarMetadata](#calendarmetadata)
  - [CalendarMetadataRequest / CalendarMetadataResponse](#calendarmetadatarequest--calendarmetadataresponse)
  - [Event Models](#event-models)
  - [Enums](#enums)
- [Mapping Strategy](#mapping-strategy)
  - [Benefits of MapStruct](#benefits-of-mapstruct)
- [API Endpoints](#api-endpoints)
  - [Create Calendar](#create-calendar)
  - [Get All Calendars](#get-all-calendars)
  - [Get Calendar by ID](#get-calendar-by-id)
  - [Delete Calendar](#delete-calendar)
- [Exception Handling](#exception-handling)
- [Health Checks](#health-checks)
  - [Health Endpoint](#health-endpoint)
  - [Kubernetes Probes](#kubernetes-probes)
- [Connection Pool Configuration](#connection-pool-configuration)
- [Technologies](#technologies)
- [Database Setup](#database-setup)
  - [Starting PostgreSQL with Docker Compose](#starting-postgresql-with-docker-compose)
  - [Stopping the Database](#stopping-the-database)
  - [Database Schema](#database-schema)
- [Additional Information](#additional-information)
  - [VS Code Settings Caveat](#vs-code-settings-caveat)
- [Architecture Benefits](#architecture-benefits)
- [Future Enhancements](#future-enhancements)

## Getting Started

### Prerequisites

- **Java 17** or higher
- **Maven 3.9+**
- **Docker** and **Docker Compose** (for database)
- **Make** (optional, for convenience commands)

### Quick Start

1. **Start the database:**

   ```bash
   make db-start
   # Or manually: docker-compose up -d
   ```

2. **Build and run the application:**

   ```bash
   make run
   # Or manually: mvn clean spring-boot:run
   ```

3. **Access Swagger UI:**
   - Open: `http://localhost:8080/swagger-ui.html`

### Local Development

#### Using Make (Recommended)

The project includes a `Makefile` with convenient commands:

```bash
# Database
make db-start          # Start PostgreSQL database
make db-stop           # Stop database
make db-status         # Check database status
make db-psql           # Connect to dev database
make db-psql-test      # Connect to test database
make db-reset          # Reset database (deletes all data)

# Application
make run               # Run Spring Boot app
make run-debug         # Run with debug port (8787)
make stop              # Stop running application

# Building
make install           # Build and install (unit tests only)
make verify            # Build and run all tests
make clean             # Clean build artifacts
```

#### Manual Commands

```bash
# Start database
docker-compose up -d

# Build project
mvn clean install

# Run application
mvn spring-boot:run

# Stop database
docker-compose down
```

### Running Tests

#### Unit Tests Only

```bash
make test
# Or: mvn test
```

Runs unit tests only (excludes integration tests by default).

#### Integration Tests Only

```bash
make test-integration
# Or: mvn test -Pintegration-tests
```

**Note:** Integration tests require:

- Database to be running (`make db-start`)
- Test database to exist (created automatically by `db-start`)

#### All Tests

```bash
make test-all
# Or: mvn test -Pintegration-tests
```

#### Test Database

Integration tests use a separate database (`calendardb_test`) to avoid polluting development data:

- **Dev database**: `calendardb` (port 5432)
- **Test database**: `calendardb_test` (same server, different database)

The test database is automatically created when you run `make db-start`. If needed, you can create it manually:

```bash
make db-setup-test
```

## Architecture Overview

This application follows a **layered architecture** pattern with clear separation of concerns:

```none
┌─────────────────────────────────────┐
│           Controller Layer          │
├─────────────────────────────────────┤
│  - Receives HTTP requests (XML)     │
│  - Returns HTTP responses (JSON)    │
│  - Uses Request/Response DTOs       │
└─────────────────┬───────────────────┘
                  │
                  ▼
┌─────────────────────────────────────┐
│            Service Layer            │
├─────────────────────────────────────┤
│  - Business logic                   │
│  - Transforms Request DTO → Entity  │
│  - Transforms Entity → Response DTO │
│  - Uses MapStruct mappers           │
└─────────────────┬───────────────────┘
                  │
                  ▼
┌─────────────────────────────────────┐
│            Manager Layer            │
├─────────────────────────────────────┤
│  - Abstraction over persistence     │
│  - Works only with Entity objects   │
│  - No knowledge of DTOs             │
└─────────────────┬───────────────────┘
                  │
                  ▼
┌─────────────────────────────────────┐
│           Repository Layer          │
├─────────────────────────────────────┤
│  - Data persistence (JDBC)          │
│  - Database operations              │
│  - Works only with Entity objects   │
└─────────────────────────────────────┘
```

## Data Flow

### Create Calendar Flow

1. **Controller** receives `CalendarRequest` (XML format)
2. **Service** validates and transforms `CalendarRequest` → `Calendar` using `CalendarRequestMapper`
3. **Service** calls **Manager** to save the entity
4. **Manager** delegates to **Repository** which saves `Calendar` to database
5. **Service** transforms `Calendar` → `CalendarResponse` using `CalendarResponseMapper`
6. **Controller** returns `ResponseEntity<CalendarResponse>` with:
   - Status: 201 Created
   - Location header: `/api/calendars/{id}`
   - Body: `CalendarResponse` (JSON format)

### Retrieve Calendar Flow

1. **Controller** receives request with optional ID parameter
2. **Service** calls **Manager** to fetch `Calendar`(s)
3. **Manager** delegates to **Repository** which queries the database
4. **Service** transforms `Calendar` → `CalendarResponse` using `CalendarResponseMapper`
5. **Controller** returns `ResponseEntity<CalendarResponse>` or `ResponseEntity<List<CalendarResponse>>` (JSON format)

### Delete Calendar Flow

1. **Controller** receives DELETE request with ID parameter
2. **Service** calls **Manager** to delete `Calendar` by ID
3. **Manager** delegates to **Repository** which deletes from database
4. **Controller** returns `ResponseEntity<Void>` with status 204 No Content (idempotent)

## Data Models

### CalendarRequest

- **Purpose**: Represents incoming XML data from clients
- **Format**: XML (consumes `application/xml`)
- **Annotations**: `@JacksonXmlRootElement`, `@JacksonXmlProperty`
- **Location**: Controller layer
- **Structure**: Contains nested `CalendarMetadataRequest` (with status, visibility, timestamps) and `List<EventRequest>` (with name, description, type, dates, location)

### Calendar

- **Purpose**: Internal domain model for persistence
- **Format**: Plain Java object
- **Location**: Manager/Repository layer (entity)
- **Note**: This is what gets saved to the database
- **Structure**: Contains nested `CalendarMetadata` (with status, visibility, timestamps, count) and `List<CalendarEvent>` (with name, description, type, dates, location)

### CalendarResponse

- **Purpose**: Represents outgoing JSON data to clients
- **Format**: JSON (produces `application/json`)
- **Annotations**: `@JsonFormat` for date/time formatting
- **Location**: Controller layer
- **Structure**: Contains nested `CalendarMetadataResponse` and `List<EventResponse>`

### CalendarMetadata

- **Purpose**: Entity representing calendar metadata (status, visibility, timestamps, count)
- **Location**: Persistence layer (entity)

### CalendarMetadataRequest / CalendarMetadataResponse

- **Purpose**: DTOs for calendar metadata in requests and responses
- **Location**: DTO layer

### Event Models

- **EventRequest/EventResponse/CalendarEvent**: Calendar event structure with:
  - `id` (String): Event UUID
  - `name` (String): Event name
  - `description` (String): Event description
  - `type` (EventType enum): Event type (HOLIDAY, MEETING, APPOINTMENT, REMINDER, OTHER)
  - `disabled` (Boolean): Whether event is disabled
  - `allDay` (Boolean): Whether event is all-day
  - `startDateTime` (LocalDateTime): Event start date and time
  - `endDateTime` (LocalDateTime): Event end date and time
  - `location` (String): Event location
  - Timestamps: `createdAt`, `createdBy`, `updatedAt`, `updatedBy`

### Enums

- **CalendarState**: UNKNOWN, ACTIVE, INACTIVE (maps to lowercase XML values: "unknown", "active", "inactive")
- **CalendarVisibility**: PERSONAL, SHARED, PRIVATE (maps to lowercase XML values: "personal", "shared", "private")
- **EventType**: HOLIDAY, MEETING, APPOINTMENT, REMINDER, OTHER (maps to lowercase XML values)

## Mapping Strategy

The application uses **MapStruct** for compile-time, type-safe mapping between DTOs and entities:

- **CalendarRequestMapper**: Converts between `CalendarRequest` ↔ `Calendar`
  - Maps nested `CalendarMetadataRequest` ↔ `CalendarMetadata`
  - Maps `List<EventRequest>` ↔ `List<CalendarEvent>`
- **CalendarResponseMapper**: Converts between `Calendar` ↔ `CalendarResponse`
  - Maps nested `CalendarMetadata` ↔ `CalendarMetadataResponse`
  - Maps `List<CalendarEvent>` ↔ `List<EventResponse>`

### Benefits of MapStruct

- Compile-time code generation (no runtime overhead)
- Type-safe mappings
- Works seamlessly with Lombok
- IDE support and debugging
- Handles nested structures automatically

## API Endpoints

### Create Calendar

```http
POST /api/calendars
Content-Type: application/xml
Accept: application/json
```

**Request Body (XML):**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<calendar id="20dbf44a-b88b-4742-a0b0-1d6c7dece68d">
    <name>Work Calendar</name>
    <description>Work schedule and meetings</description>
    <metadata>
        <status>active</status>
        <visibility>shared</visibility>
        <created-at>11/13/2025 12:00:00</created-at>
        <created-by>John Doe</created-by>
        <updated-at>11/13/2025 14:30:00</updated-at>
        <updated-by>Jane Doe</updated-by>
        <count>2</count>
    </metadata>
    <event id="a1b2c3d4-e5f6-7890-abcd-111111111111">
        <name>Team Standup</name>
        <description>Daily team standup meeting</description>
        <type>meeting</type>
        <disabled>false</disabled>
        <all-day>false</all-day>
        <start-datetime>11/14/2025 09:00:00</start-datetime>
        <end-datetime>11/14/2025 09:30:00</end-datetime>
        <location>Zoom</location>
        <created-at>11/10/2025 10:00:00</created-at>
        <created-by>John Doe</created-by>
    </event>
</calendar>
```

**Response (JSON):**

- **Status**: 201 Created
- **Location Header**: `/api/calendars/{id}`

```json
{
  "id": "20dbf44a-b88b-4742-a0b0-1d6c7dece68d",
  "name": "Work Calendar",
  "description": "Work schedule and meetings",
  "metadata": {
    "status": "active",
    "visibility": "shared",
    "createdAt": "11/13/2025 12:00:00",
    "createdBy": "John Doe",
    "updatedAt": "11/13/2025 14:30:00",
    "updatedBy": "Jane Doe",
    "count": 2
  },
  "events": [
    {
      "id": "a1b2c3d4-e5f6-7890-abcd-111111111111",
      "name": "Team Standup",
      "description": "Daily team standup meeting",
      "type": "meeting",
      "disabled": false,
      "allDay": false,
      "startDateTime": "2025-11-14T09:00:00",
      "endDateTime": "2025-11-14T09:30:00",
      "location": "Zoom",
      "createdAt": "11/10/2025 10:00:00",
      "createdBy": "John Doe"
    }
  ]
}
```

### Get All Calendars

```http
GET /api/calendars?page=0&size=10
Accept: application/json
```

**Query Parameters:**

- `page` (optional, default: 0): Page number (0-indexed)
- `size` (optional, default: 10): Number of items per page (max: 100)

**Response (JSON):**

Returns a paginated response containing calendars with their events and pagination metadata.

```json
{
  "items": [
    {
      "id": "20dbf44a-b88b-4742-a0b0-1d6c7dece68d",
      "name": "Work Calendar",
      "description": "Work schedule and meetings",
      "metadata": { ... },
      "events": [ ... ]
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 42,
  "totalPages": 5,
  "first": true,
  "last": false
}
```

### Get Calendar by ID

```http
GET /api/calendars/{id}
Accept: application/json
```

**Response (JSON):**

Returns a single calendar with its metadata and events.

### Delete Calendar

```http
DELETE /api/calendars/{id}
```

**Response:**

- **Status**: 204 No Content
- **Note**: Idempotent operation - returns 204 whether the resource existed or not

## Exception Handling

The application includes a global exception handler using `@ControllerAdvice` that returns standardized `ProblemDetail` responses (RFC 7807):

- `CalendarNotFoundException`: Returns 404 Not Found with ProblemDetail
- `CalendarAlreadyExistsException`: Returns 400 Bad Request with ProblemDetail
- `DuplicateKeyException`: Returns 409 Conflict with ProblemDetail
- `MethodArgumentNotValidException`: Returns 400 Bad Request with validation error details
- `HttpMessageNotReadableException`: Returns 400 Bad Request for XML parsing errors

All controller methods return `ResponseEntity` wrappers for explicit HTTP status code and header control.

## Health Checks

The application includes Spring Boot Actuator health checks for monitoring:

### Health Endpoint

```http
GET /actuator/health
```

**Response:**

- **Status**: 200 OK when healthy
- **Details**: Database connectivity, disk space, and other health indicators

### Kubernetes Probes

The health endpoint supports Kubernetes liveness and readiness probes:

- **Liveness**: `/actuator/health/liveness`
- **Readiness**: `/actuator/health/readiness`

Health details are shown when authorized (configured via `management.endpoint.health.show-details`).

## Connection Pool Configuration

The application uses HikariCP for database connection pooling with production-ready settings:

- **Minimum Idle**: 5 connections
- **Maximum Pool Size**: 20 connections
- **Connection Timeout**: 30 seconds
- **Idle Timeout**: 10 minutes
- **Max Lifetime**: 30 minutes
- **Leak Detection**: 60 seconds threshold

Connection pool metrics are logged at INFO level for monitoring.

## Technologies

- **Spring Boot 3.5.6**: Application framework
- **Spring Boot Actuator**: Health checks and monitoring
- **HikariCP**: High-performance JDBC connection pool
- **Jackson XML**: XML serialization/deserialization (replaces JAXB)
- **MapStruct 1.6.3**: DTO mapping
- **Lombok**: Boilerplate code reduction
- **SpringDoc OpenAPI 2.8.14**: API documentation (Swagger UI)
- **Java 17**: Programming language

## Database Setup

The application uses PostgreSQL with Flyway for database migrations. A Docker Compose file is provided for easy database setup.

### Starting PostgreSQL with Docker Compose

**Using Make (Recommended):**

```bash
make db-start
```

**Manual:**

```bash
docker-compose up -d
```

The database will be available at:

- **Host**: `localhost`
- **Port**: `5432`
- **Database**: `calendardb` (dev) / `calendardb_test` (tests)
- **Username**: `calendaruser`
- **Password**: `calendarpass`

**Note:** The `db-start` command automatically creates the test database (`calendardb_test`) if it doesn't exist. Flyway will automatically run migrations when the application starts, creating the `calendars` and `events` tables in both databases.

### Stopping the Database

```bash
docker-compose down
```

To also remove the data volume:

```bash
docker-compose down -v
```

### Database Schema

- **calendars**: Stores calendar information with metadata (status, visibility, timestamps, etc.)
- **events**: Stores events linked to calendars via foreign key relationship

## Additional Information

### VS Code Settings Caveat

If you have the following setting in your VS Code settings (`.vscode/settings.json`):

```json
{
    "java.configuration.updateBuildConfiguration": "automatic"
}
```

If you run the build and run commands separately, you may encounter this error:

```none
***************************
APPLICATION FAILED TO START
***************************

Description:

Parameter 1 of constructor in org.example.CalendarServiceImpl required a bean of type 'org.example.CalendarRequestMapper' that could not be found.

Action:

Consider defining a bean of type 'org.example.CalendarRequestMapper' in your configuration.
```

Instead, you may need to run this single command:

```bash
mvn clean spring-boot:run
```

This ensures annotation processors (MapStruct, Lombok) are properly executed and their generated code is found.

## Architecture Benefits

1. **Separation of Concerns**: Each layer has a single responsibility
2. **API Independence**: Request/Response DTOs can evolve without affecting entities
3. **Database Independence**: Entities can change without breaking API contracts
4. **Type Safety**: Compile-time mapping prevents runtime errors
5. **Testability**: Each layer can be tested independently
6. **Maintainability**: Clear boundaries make the codebase easier to understand and modify

## Future Enhancements

- [x] Replace in-memory storage with a real database (JPA/Hibernate)
  - Using JDBC with PostgreSQL
- [ ] Add validation annotations to DTOs
- [x] Implement pagination for list endpoints
  - Pagination support added to GET /api/calendars
- [ ] Add authentication/authorization
- [ ] Implement caching layer
- [ ] Add comprehensive unit and integration tests
