package org.example.controller;

import org.example.dto.request.CalendarRequest;
import org.example.dto.response.CalendarResponse;
import org.example.dto.response.PagedResponse;
import org.example.exception.CalendarNotFoundException;
import org.example.service.CalendarService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/calendars")
@RequiredArgsConstructor
public class CalendarController {
    private final CalendarService calendarService;

    @PostMapping(
        path = "",
        consumes = MediaType.APPLICATION_XML_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
        summary = "Create calendar",
        description = "Creates a new calendar entry from XML input",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Calendar request in XML format",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_XML_VALUE,
                schema = @Schema(implementation = CalendarRequest.class),
                examples = @ExampleObject(
                    name = "Example Calendar",
                    value = """
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
                            <updated-at>11/13/2025 14:30:00</updated-at>
                            <updated-by>Jane Doe</updated-by>
                        </event>
                        <event id="a1b2c3d4-e5f6-7890-abcd-222222222222">
                            <name>Thanksgiving</name>
                            <description>Thanksgiving holiday</description>
                            <type>holiday</type>
                            <disabled>false</disabled>
                            <all-day>true</all-day>
                            <start-datetime>11/27/2025 00:00:00</start-datetime>
                            <end-datetime>11/27/2025 23:59:59</end-datetime>
                            <created-at>11/01/2025 08:00:00</created-at>
                            <created-by>John Doe</created-by>
                            <updated-at>11/13/2025 14:30:00</updated-at>
                            <updated-by>John Doe</updated-by>
                        </event>
                    </calendar>
                    """
                )
            )
        )
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Calendar created successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CalendarResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - calendar with the same ID already exists",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        )
    })
    /**
     * Creates a new calendar entry from XML input.
     *
     * @param calendar the calendar request in XML format
     * @return ResponseEntity with status 201 (CREATED), Location header, and the created calendar response
     */
    public ResponseEntity<CalendarResponse> createCalendar(@Valid @RequestBody CalendarRequest calendar) {
        log.info("POST /api/calendars - Creating calendar: id={}, name={}", 
            calendar.getId(), calendar.getName());
        CalendarResponse response = calendarService.create(calendar);
        var location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.getId())
            .toUri();
        log.info("POST /api/calendars - Calendar created: id={}, status=201, location={}", 
            response.getId(), location);
        return ResponseEntity.status(HttpStatus.CREATED)
            .location(location)
            .body(response);
    }

    @GetMapping("")
    @Operation(
        summary = "Get all calendars",
        description = "Retrieves a paginated list of calendar entries. Use page and size query parameters for pagination."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved list of calendars",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = PagedResponse.class)
            )
        )
    })
    /**
     * Retrieves calendar entries with pagination support.
     *
     * @param page the page number (0-indexed, default: 0)
     * @param size the page size (default: 10)
     * @return ResponseEntity with status 200 (OK) and a paginated response containing calendar responses
     */
    public ResponseEntity<PagedResponse<CalendarResponse>> getCalendars(
        @Parameter(description = "Page number (0-indexed)", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size", example = "10")
        @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/calendars - Retrieving calendars: page={}, size={}", page, size);
        
        // Validate pagination parameters
        if (page < 0) {
            log.warn("Invalid page number: {}, defaulting to 0", page);
            page = 0;
        }
        if (size < 1) {
            log.warn("Invalid page size: {}, defaulting to 10", size);
            size = 10;
        }
        if (size > 100) {
            log.warn("Page size too large: {}, capping at 100", size);
            size = 100;
        }
        
        PagedResponse<CalendarResponse> response = calendarService.findAll(page, size);
        log.info("GET /api/calendars - Retrieved {} calendar(s) on page {} of {}, status=200", 
            response.getItems().size(), page, response.getTotalPages());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get calendar by ID",
        description = "Retrieves a specific calendar entry by its ID"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved calendar",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CalendarResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Calendar not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        )
    })
    /**
     * Retrieves a specific calendar entry by its ID.
     *
     * @param id the calendar ID
     * @return ResponseEntity with status 200 (OK) and the calendar response
     * @throws CalendarNotFoundException if the calendar with the given ID is not found
     */
    public ResponseEntity<CalendarResponse> getCalendar(
        @Parameter(description = "Calendar ID", required = true, example = "20dbf44a-b88b-4742-a0b0-1d6c7dece68d")
        @PathVariable String id) {
        log.info("GET /api/calendars/{} - Retrieving calendar", id);
        CalendarResponse response = calendarService.findById(id)
            .orElseThrow(() -> {
                log.warn("GET /api/calendars/{} - Calendar not found, status=404", id);
                return new CalendarNotFoundException(id);
            });
        log.info("GET /api/calendars/{} - Calendar retrieved: name={}, status=200", 
            id, response.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete calendar",
        description = "Deletes a calendar entry by ID. Idempotent - returns 204 whether the resource existed or not."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Calendar deleted successfully (or did not exist)"
        )
    })
    /**
     * Deletes a calendar entry by ID. Idempotent operation - returns 204 whether the resource existed or not.
     *
     * @param id the calendar ID to delete
     * @return ResponseEntity with status 204 (NO_CONTENT)
     */
    public ResponseEntity<Void> deleteCalendar(
        @Parameter(description = "Calendar ID", required = true, example = "20dbf44a-b88b-4742-a0b0-1d6c7dece68d")
        @PathVariable String id) {
        log.info("DELETE /api/calendars/{} - Deleting calendar", id);
        calendarService.deleteById(id);
        log.info("DELETE /api/calendars/{} - Calendar deleted, status=204", id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("")
    @Operation(
        summary = "Delete all calendars",
        description = "Deletes all calendar entries from the database. This operation cannot be undone."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "All calendars deleted successfully"
        )
    })
    /**
     * Deletes all calendar entries from the database.
     *
     * @return ResponseEntity with status 204 (NO_CONTENT)
     */
    public ResponseEntity<Void> deleteAllCalendars() {
        log.info("DELETE /api/calendars - Deleting all calendars");
        calendarService.deleteAll();
        log.info("DELETE /api/calendars - All calendars deleted, status=204");
        return ResponseEntity.noContent().build();
    }
}

