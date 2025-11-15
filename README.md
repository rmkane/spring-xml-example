<!-- omit in toc -->
# Spring XML Example

A Spring Boot application demonstrating a layered architecture with XML request handling and JSON response formatting, using DTOs, entities, and MapStruct for type-safe mapping. Features nested data structures (info sections) and simplified entry objects with enum types.

<!-- omit in toc -->
## Table of Contents

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
- [Technologies](#technologies)
- [Database Setup](#database-setup)
  - [Starting PostgreSQL with Docker Compose](#starting-postgresql-with-docker-compose)
  - [Stopping the Database](#stopping-the-database)
  - [Database Schema](#database-schema)
- [Running the Application](#running-the-application)
  - [Prerequisites](#prerequisites)
  - [Build and Run](#build-and-run)
    - [VS Code Settings Caveat](#vs-code-settings-caveat)
  - [Access Swagger UI](#access-swagger-ui)
- [Architecture Benefits](#architecture-benefits)
- [Future Enhancements](#future-enhancements)

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
GET /api/calendars
Accept: application/json
```

**Response (JSON):**

Returns a list of all calendars with their events.

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

All controller methods return `ResponseEntity` wrappers for explicit HTTP status code and header control.

## Technologies

- **Spring Boot 3.5.6**: Application framework
- **Jackson XML**: XML serialization/deserialization (replaces JAXB)
- **MapStruct 1.6.3**: DTO mapping
- **Lombok**: Boilerplate code reduction
- **SpringDoc OpenAPI 2.8.14**: API documentation (Swagger UI)
- **Java 17**: Programming language

## Database Setup

The application uses PostgreSQL with Flyway for database migrations. A Docker Compose file is provided for easy database setup.

### Starting PostgreSQL with Docker Compose

1. Start the PostgreSQL database:

   ```bash
   docker-compose up -d
   ```

2. Verify the database is running:

   ```bash
   docker-compose ps
   ```

3. The database will be available at:

   - **Host**: `localhost`
   - **Port**: `5432`
   - **Database**: `calendardb`
   - **Username**: `calendaruser`
   - **Password**: `calendarpass`

4. Flyway will automatically run migrations when the application starts, creating the `calendars` and `events` tables.

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

## Running the Application

### Prerequisites

- Java 17 or higher
- Maven 3.9+

### Build and Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

#### VS Code Settings Caveat

If you have the following setting in your VS Code settings (`.vscode/settings.json`):

```json
{
    "java.configuration.updateBuildConfiguration": "automatic"
}
```

If you run the build and run commands separately (as shown above), you may encounter this error:

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

### Access Swagger UI

Once running, visit: `http://localhost:8080/swagger-ui.html`

## Architecture Benefits

1. **Separation of Concerns**: Each layer has a single responsibility
2. **API Independence**: Request/Response DTOs can evolve without affecting entities
3. **Database Independence**: Entities can change without breaking API contracts
4. **Type Safety**: Compile-time mapping prevents runtime errors
5. **Testability**: Each layer can be tested independently
6. **Maintainability**: Clear boundaries make the codebase easier to understand and modify

## Future Enhancements

- ~~Replace in-memory storage with a real database (JPA/Hibernate)~~ ✅ Done - Using JDBC with PostgreSQL
- Add validation annotations to DTOs
- Implement pagination for list endpoints
- Add authentication/authorization
- Implement caching layer
- Add comprehensive unit and integration tests
