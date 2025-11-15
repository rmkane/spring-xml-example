<!-- omit in toc -->
# Spring XML Example

A Spring Boot application demonstrating a layered architecture with XML request handling and JSON response formatting, using DTOs, entities, and MapStruct for type-safe mapping. Features nested data structures (info sections) and simplified entry objects with enum types.

<!-- omit in toc -->
## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Data Flow](#data-flow)
  - [Create Metadata Flow](#create-metadata-flow)
  - [Retrieve Metadata Flow](#retrieve-metadata-flow)
  - [Delete Metadata Flow](#delete-metadata-flow)
- [Data Models](#data-models)
  - [MetadataRequest](#metadatarequest)
  - [MetadataEntity](#metadataentity)
  - [MetadataResponse](#metadataresponse)
  - [Entry Models](#entry-models)
  - [Enums](#enums)
- [Mapping Strategy](#mapping-strategy)
  - [Benefits of MapStruct](#benefits-of-mapstruct)
- [API Endpoints](#api-endpoints)
  - [Create Metadata](#create-metadata)
  - [Get All Metadata](#get-all-metadata)
  - [Get Metadata by ID](#get-metadata-by-id)
  - [Delete Metadata](#delete-metadata)
- [Exception Handling](#exception-handling)
- [Technologies](#technologies)
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
│           Repository Layer          │
├─────────────────────────────────────┤
│  - Data persistence                 │
│  - Works only with Entity objects   │
│  - No knowledge of DTOs             │
└─────────────────────────────────────┘
```

## Data Flow

### Create Metadata Flow

1. **Controller** receives `MetadataRequest` (XML format)
2. **Service** validates and transforms `MetadataRequest` → `MetadataEntity` using `MetadataRequestMapper`
3. **Repository** saves `MetadataEntity` to storage
4. **Service** transforms `MetadataEntity` → `MetadataResponse` using `MetadataResponseMapper`
5. **Controller** returns `ResponseEntity<MetadataResponse>` with:
   - Status: 201 Created
   - Location header: `/api/metadata/{id}`
   - Body: `MetadataResponse` (JSON format)

### Retrieve Metadata Flow

1. **Controller** receives request with optional ID parameter
2. **Service** calls **Repository** to fetch `MetadataEntity`(s)
3. **Service** transforms `MetadataEntity` → `MetadataResponse` using `MetadataResponseMapper`
4. **Controller** returns `ResponseEntity<MetadataResponse>` or `ResponseEntity<List<MetadataResponse>>` (JSON format)

### Delete Metadata Flow

1. **Controller** receives DELETE request with ID parameter
2. **Service** calls **Repository** to delete `MetadataEntity` by ID
3. **Controller** returns `ResponseEntity<Void>` with status 204 No Content (idempotent)

## Data Models

### MetadataRequest

- **Purpose**: Represents incoming XML data from clients
- **Format**: XML (consumes `application/xml`)
- **Annotations**: `@JacksonXmlRootElement`, `@JacksonXmlProperty`
- **Location**: Controller layer
- **Structure**: Contains nested `InfoRequest` (with status, dates) and `List<EntryRequest>` (with name, count, type)

### MetadataEntity

- **Purpose**: Internal domain model for persistence
- **Format**: Plain Java object
- **Location**: Repository layer
- **Note**: This is what gets saved to the database/storage
- **Structure**: Contains nested `InfoEntity` and `List<EntryEntity>`

### MetadataResponse

- **Purpose**: Represents outgoing JSON data to clients
- **Format**: JSON (produces `application/json`)
- **Annotations**: `@JsonFormat` for date/time formatting
- **Location**: Controller layer
- **Structure**: Contains nested `InfoResponse` and `List<EntryResponse>`

### Entry Models

- **EntryRequest/EntryResponse/EntryEntity**: Simplified structure with:
  - `name` (String): Entry name
  - `count` (Integer): Entry count
  - `type` (EntryType enum): Entry type (STANDARD, PREMIUM, BASIC)

### Enums

- **CalendarState**: UNKNOWN, ACTIVE, INACTIVE (maps to lowercase XML values)
- **EntryType**: STANDARD, PREMIUM, BASIC (maps to lowercase XML values)

## Mapping Strategy

The application uses **MapStruct** for compile-time, type-safe mapping between DTOs and entities:

- **MetadataRequestMapper**: Converts between `MetadataRequest` ↔ `MetadataEntity`
  - Maps nested `InfoRequest` ↔ `InfoEntity`
  - Maps `List<EntryRequest>` ↔ `List<EntryEntity>`
- **MetadataResponseMapper**: Converts between `MetadataEntity` ↔ `MetadataResponse`
  - Maps nested `InfoEntity` ↔ `InfoResponse`
  - Maps `List<EntryEntity>` ↔ `List<EntryResponse>`

### Benefits of MapStruct

- Compile-time code generation (no runtime overhead)
- Type-safe mappings
- Works seamlessly with Lombok
- IDE support and debugging
- Handles nested structures automatically

## API Endpoints

### Create Metadata

```http
POST /api/metadata
Content-Type: application/xml
Accept: application/json
```

**Request Body (XML):**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<metadata id="20dbf44a-b88b-4742-a0b0-1d6c7dece68d">
    <name>Example Metadata</name>
    <description>This is an example</description>
    <info>
        <status>active</status>
        <created-date>01/15/2025</created-date>
        <created-time>14:30:00</created-time>
        <created-datetime>01/15/2025 14:30:00</created-datetime>
    </info>
    <entry>
        <name>Entry 1</name>
        <count>10</count>
        <type>standard</type>
    </entry>
    <entry>
        <name>Entry 2</name>
        <count>5</count>
        <type>premium</type>
    </entry>
</metadata>
```

**Response (JSON):**

- **Status**: 201 Created
- **Location Header**: `/api/metadata/{id}`

```json
{
  "id": "20dbf44a-b88b-4742-a0b0-1d6c7dece68d",
  "name": "Example Metadata",
  "description": "This is an example",
  "info": {
    "status": "active",
    "createdDate": "01/15/2025",
    "createdTime": "14:30:00",
    "createdDatetime": "01/15/2025 14:30:00"
  },
  "entries": [
    {
      "name": "Entry 1",
      "count": 10,
      "type": "standard"
    },
    {
      "name": "Entry 2",
      "count": 5,
      "type": "premium"
    }
  ]
}
```

### Get All Metadata

```http
GET /api/metadata
Accept: application/json
```

**Response (JSON):**

```json
[
  {
    "id": "1",
    "name": "Example Metadata",
    ...
  }
]
```

### Get Metadata by ID

```http
GET /api/metadata/{id}
Accept: application/json
```

**Response (JSON):**

```json
{
  "id": "20dbf44a-b88b-4742-a0b0-1d6c7dece68d",
  "name": "Example Metadata",
  "description": "This is an example",
  "info": {
    "status": "active",
    "createdDate": "01/15/2025",
    "createdTime": "14:30:00",
    "createdDatetime": "01/15/2025 14:30:00"
  },
  "entries": [
    {
      "name": "Entry 1",
      "count": 10,
      "type": "standard"
    }
  ]
}
```

### Delete Metadata

```http
DELETE /api/metadata/{id}
```

**Response:**

- **Status**: 204 No Content
- **Note**: Idempotent operation - returns 204 whether the resource existed or not

## Exception Handling

The application includes a global exception handler using `@ControllerAdvice` that returns standardized `ProblemDetail` responses (RFC 7807):

- `MetadataNotFoundException`: Returns 404 Not Found with ProblemDetail
- `MetadataAlreadyExistsException`: Returns 400 Bad Request with ProblemDetail

All controller methods return `ResponseEntity` wrappers for explicit HTTP status code and header control.

## Technologies

- **Spring Boot 3.5.6**: Application framework
- **Jackson XML**: XML serialization/deserialization (replaces JAXB)
- **MapStruct 1.6.3**: DTO mapping
- **Lombok**: Boilerplate code reduction
- **SpringDoc OpenAPI 2.8.14**: API documentation (Swagger UI)
- **Java 17**: Programming language

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

Parameter 1 of constructor in org.example.MetadataServiceImpl required a bean of type 'org.example.MetadataRequestMapper' that could not be found.

Action:

Consider defining a bean of type 'org.example.MetadataRequestMapper' in your configuration.
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

- Replace in-memory storage with a real database (JPA/Hibernate)
- Add validation annotations to DTOs
- Implement pagination for list endpoints
- Add authentication/authorization
- Implement caching layer
- Add comprehensive unit and integration tests
