package org.example;

import java.time.*;
import java.util.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.xml.annotation.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import io.swagger.v3.oas.annotations.responses.*;

import lombok.*;
import lombok.Builder;
import lombok.extern.slf4j.*;

import org.mapstruct.*;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/* -- Application layer -- */

@SpringBootApplication
public class App {
    /**
     * Main entry point for the Spring Boot application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}

/* -- Controller layer -- */

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
class AppController {
    private final MetadataService metadataService;

    @PostMapping(
        path = "/metadata",
        consumes = MediaType.APPLICATION_XML_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
        summary = "Create metadata",
        description = "Creates a new metadata entry from XML input",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Metadata request in XML format",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_XML_VALUE,
                schema = @Schema(implementation = MetadataRequest.class),
                examples = @ExampleObject(
                    name = "Example Metadata",
                    value = """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <metadata id="012345678-9012-3456-7890-123456789012">
                        <name>Example Metadata</name>
                        <description>This is an example</description>
                        <state>active</state>
                        <created-date>01/15/2025</created-date>
                        <created-time>14:30:00</created-time>
                        <created-datetime>01/15/2025 14:30:00</created-datetime>
                    </metadata>
                    """
                )
            )
        )
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Metadata created successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = MetadataResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - metadata with the same ID already exists",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        )
    })
    /**
     * Creates a new metadata entry from XML input.
     *
     * @param metadata the metadata request in XML format
     * @return ResponseEntity with status 201 (CREATED), Location header, and the created metadata response
     */
    public ResponseEntity<MetadataResponse> createMetadata(@RequestBody MetadataRequest metadata) {
        MetadataResponse response = metadataService.create(metadata);
        var location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.getId())
            .toUri();
        return ResponseEntity.status(HttpStatus.CREATED)
            .location(location)
            .body(response);
    }

    @GetMapping("/metadata")
    @Operation(
        summary = "Get all metadata",
        description = "Retrieves a list of all metadata entries"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved list of metadata",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = MetadataResponse.class))
            )
        )
    })
    /**
     * Retrieves all metadata entries.
     *
     * @return ResponseEntity with status 200 (OK) and a list of all metadata responses
     */
    public ResponseEntity<List<MetadataResponse>> getMetadata() {
        List<MetadataResponse> response = metadataService.findAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/metadata/{id}")
    @Operation(
        summary = "Get metadata by ID",
        description = "Retrieves a specific metadata entry by its ID"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved metadata",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = MetadataResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Metadata not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        )
    })
    /**
     * Retrieves a specific metadata entry by its ID.
     *
     * @param id the metadata ID
     * @return ResponseEntity with status 200 (OK) and the metadata response
     * @throws MetadataNotFoundException if the metadata with the given ID is not found
     */
    public ResponseEntity<MetadataResponse> getMetadata(
        @Parameter(description = "Metadata ID", required = true, example = "012345678-9012-3456-7890-123456789012")
        @PathVariable String id) {
        MetadataResponse response = metadataService.findById(id)
            .orElseThrow(() -> new MetadataNotFoundException(id));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/metadata/{id}")
    @Operation(
        summary = "Delete metadata",
        description = "Deletes a metadata entry by ID. Idempotent - returns 204 whether the resource existed or not."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Metadata deleted successfully (or did not exist)"
        )
    })
    /**
     * Deletes a metadata entry by ID. Idempotent operation - returns 204 whether the resource existed or not.
     *
     * @param id the metadata ID to delete
     * @return ResponseEntity with status 204 (NO_CONTENT)
     */
    public ResponseEntity<Void> deleteMetadata(
        @Parameter(description = "Metadata ID", required = true, example = "012345678-9012-3456-7890-123456789012")
        @PathVariable String id) {
        metadataService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

/* -- Service layer -- */

interface MetadataService {
    /**
     * Finds a metadata entry by its ID.
     *
     * @param id the metadata ID
     * @return Optional containing the metadata response if found, empty otherwise
     */
    Optional<MetadataResponse> findById(String id);

    /**
     * Deletes a metadata entry by its ID. Idempotent operation.
     *
     * @param id the metadata ID to delete
     */
    void deleteById(String id);

    /**
     * Retrieves all metadata entries.
     *
     * @return list of all metadata responses
     */
    List<MetadataResponse> findAll();

    /**
     * Creates a new metadata entry. Generates an ID if not provided.
     *
     * @param metadata the metadata request
     * @return the created metadata response
     * @throws MetadataAlreadyExistsException if a metadata entry with the same ID already exists
     */
    MetadataResponse create(MetadataRequest metadata);
}

@Service
@RequiredArgsConstructor
class MetadataServiceImpl implements MetadataService {
    private final MetadataRepository metadataRepository;
    private final MetadataRequestMapper metadataRequestMapper;
    private final MetadataResponseMapper metadataResponseMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<MetadataResponse> findById(String id) {
        return metadataRepository.findById(id)
            .map(metadataResponseMapper::toResponse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteById(String id) {
        metadataRepository.deleteById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MetadataResponse> findAll() {
        return metadataRepository.findAll()
            .stream()
            .map(metadataResponseMapper::toResponse)
            .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MetadataResponse create(MetadataRequest metadata) {
        if (metadataRepository.findById(metadata.getId()).isPresent()) {
            throw new MetadataAlreadyExistsException(metadata.getId());
        }
        if (metadata.getId() == null || metadata.getId().isEmpty()) {
            metadata.setId(generateId());
        }
        if (metadata.getState() == null) {
            metadata.setState(MetadataState.UNKNOWN);
        }
        return metadataResponseMapper.toResponse(metadataRepository.save(metadataRequestMapper.toEntity(metadata)));
    }

    /**
     * Generates a unique UUID for metadata entries.
     *
     * @return a UUID string
     */
    private String generateId() {
        return UUID.randomUUID().toString();
    }
}

/* -- Repository layer -- */

interface Repository<T, ID> {
    /**
     * Saves an entity.
     *
     * @param entity the entity to save
     * @return the saved entity
     */
    T save(T entity);

    /**
     * Deletes an entity by its ID.
     *
     * @param id the entity ID
     */
    void deleteById(ID id);

    /**
     * Finds an entity by its ID.
     *
     * @param id the entity ID
     * @return Optional containing the entity if found, empty otherwise
     */
    Optional<T> findById(ID id);

    /**
     * Retrieves all entities.
     *
     * @return list of all entities
     */
    List<T> findAll();
}

interface MetadataRepository extends Repository<MetadataEntity, String> {
    /**
     * {@inheritDoc}
     */
    @Override
    MetadataEntity save(MetadataEntity entity);

    /**
     * {@inheritDoc}
     */
    @Override
    void deleteById(String id);

    /**
     * {@inheritDoc}
     */
    @Override
    Optional<MetadataEntity> findById(String id);

    /**
     * {@inheritDoc}
     */
    @Override
    List<MetadataEntity> findAll();
}

@Component
class MetadataRepositoryImpl implements MetadataRepository {
    private List<MetadataEntity> metadataList = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public MetadataEntity save(MetadataEntity entity) {
        metadataList.add(entity);
        return entity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteById(String id) {
        metadataList.removeIf(m -> m.getId().equals(id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<MetadataEntity> findById(String id) {
        return metadataList.stream().filter(m -> m.getId().equals(id)).findFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MetadataEntity> findAll() {
        return metadataList;
    }
}

/* -- Exception handler -- */

@Slf4j
@ControllerAdvice
class GlobalExceptionHandler {
    /**
     * Handles MetadataNotFoundException and returns a ProblemDetail response.
     *
     * @param e the exception that was thrown
     * @return ProblemDetail with status 404 (NOT_FOUND) and the exception message
     */
    @ExceptionHandler(MetadataNotFoundException.class)
    public ProblemDetail handleMetadataNotFoundException(MetadataNotFoundException e) {
        log.warn("Metadata not found: {}", e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    /**
     * Handles MetadataAlreadyExistsException and returns a ProblemDetail response.
     *
     * @param e the exception that was thrown
     * @return ProblemDetail with status 400 (BAD_REQUEST) and the exception message
     */
    @ExceptionHandler(MetadataAlreadyExistsException.class)
    public ProblemDetail handleMetadataAlreadyExistsException(MetadataAlreadyExistsException e) {
        log.warn("Metadata already exists: {}", e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}

/* -- Bean configuration -- */

@Configuration
class ObjectMapperConfig {
    /**
     * Configures the Jackson ObjectMapper with JavaTimeModule and disables timestamp serialization.
     * Enum mapping is handled via @JsonProperty annotations on the enum values.
     *
     * @return configured ObjectMapper bean
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
}

/* -- Mapper layer -- */

@Mapper(componentModel = "spring")
interface MetadataRequestMapper {
    /**
     * Maps a MetadataEntity to a MetadataRequest DTO.
     *
     * @param entity the entity to map
     * @return the mapped request DTO
     */
    MetadataRequest toRequest(MetadataEntity entity);

    /**
     * Maps a MetadataRequest DTO to a MetadataEntity.
     *
     * @param request the request DTO to map
     * @return the mapped entity
     */
    MetadataEntity toEntity(MetadataRequest request);
}

@Mapper(componentModel = "spring")
interface MetadataResponseMapper {
    /**
     * Maps a MetadataEntity to a MetadataResponse DTO.
     *
     * @param entity the entity to map
     * @return the mapped response DTO
     */
    MetadataResponse toResponse(MetadataEntity entity);

    /**
     * Maps a MetadataResponse DTO to a MetadataEntity.
     *
     * @param response the response DTO to map
     * @return the mapped entity
     */
    MetadataEntity toEntity(MetadataResponse response);
}

/* -- Entity layer -- */

@Data
@NoArgsConstructor
@AllArgsConstructor
class MetadataEntity {
    private String id;
    private String name;
    private String description;
    private MetadataState state;
    private LocalDate createdDate;
    private LocalTime createdTime;
    private LocalDateTime createdDatetime;
}

/* -- DTO layer -- */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "MetadataResponse", description = "Metadata response")
class MetadataResponse {
    @Schema(description = "Metadata ID", example = "012345678-9012-3456-7890-123456789012")
    private String id;

    @Schema(description = "Metadata name", example = "Example Metadata")
    private String name;

    @Schema(description = "Metadata description", example = "This is an example")
    private String description;

    @Schema(description = "Metadata state", example = "active", allowableValues = {"unknown", "active", "inactive"})
    private MetadataState state;

    @Schema(description = "Created date", example = "01/15/2025", type = "string", format = "date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy")
    private LocalDate createdDate;

    @Schema(description = "Created time", example = "14:30:00", type = "string", format = "time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime createdTime;

    @Schema(description = "Created datetime", example = "01/15/2025 14:30:00", type = "string", format = "date-time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy HH:mm:ss")
    private LocalDateTime createdDatetime;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "metadata")
@Schema(name = "MetadataRequest", description = "Metadata request")
class MetadataRequest {
    @JacksonXmlProperty(isAttribute = true, localName = "id")
    @Schema(description = "Metadata ID", example = "1", accessMode = AccessMode.READ_WRITE)
    private String id;

    @JacksonXmlProperty(localName = "name")
    @Schema(description = "Metadata name", example = "Example Metadata")
    private String name;

    @JacksonXmlProperty(localName = "description")
    @Schema(description = "Metadata description", example = "This is an example")
    private String description;

    @JacksonXmlProperty(localName = "state")
    @Schema(description = "Metadata state", example = "active", allowableValues = {"unknown", "active", "inactive"})
    private MetadataState state;

    @JacksonXmlProperty(localName = "created-date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy")
    @Schema(description = "Created date", example = "01/15/2025", type = "string", format = "date")
    private LocalDate createdDate;

    @JacksonXmlProperty(localName = "created-time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    @Schema(description = "Created time", example = "14:30:00", type = "string", format = "time")
    private LocalTime createdTime;

    @JacksonXmlProperty(localName = "created-datetime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy HH:mm:ss")
    @Schema(description = "Created datetime", example = "01/15/2025 14:30:00", type = "string", format = "date-time")
    private LocalDateTime createdDatetime;
}

/* -- Enum layer -- */

/**
 * Metadata state enumeration.
 * Demonstrates Jackson XML enum mapping from lowercase XML values to uppercase enum constants.
 * Uses @JsonProperty to map XML values (e.g., "active") to enum constants (e.g., ACTIVE).
 */
@JsonFormat(shape = JsonFormat.Shape.STRING)
enum MetadataState {
    @JsonProperty("unknown")
    @JsonEnumDefaultValue
    UNKNOWN,
    
    @JsonProperty("active")
    ACTIVE,
    
    @JsonProperty("inactive")
    INACTIVE
}

/* -- Exception layer -- */

@ResponseStatus(HttpStatus.NOT_FOUND)
class MetadataNotFoundException extends RuntimeException {
    /**
     * Constructs a new MetadataNotFoundException with the given ID.
     *
     * @param id the metadata ID that was not found
     */
    public MetadataNotFoundException(String id) {
        super("Metadata with id " + id + " not found");
    }
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
class MetadataAlreadyExistsException extends RuntimeException {
    /**
     * Constructs a new MetadataAlreadyExistsException with the given ID.
     *
     * @param id the metadata ID that already exists
     */
    public MetadataAlreadyExistsException(String id) {
        super("Metadata with id " + id + " already exists");
    }
}