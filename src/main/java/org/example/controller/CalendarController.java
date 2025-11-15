package org.example.controller;

import java.util.List;

import org.example.dto.request.CalendarRequest;
import org.example.dto.response.CalendarResponse;
import org.example.exception.CalendarNotFoundException;
import org.example.service.CalendarService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import lombok.RequiredArgsConstructor;

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
    public ResponseEntity<CalendarResponse> createCalendar(@RequestBody CalendarRequest calendar) {
        CalendarResponse response = calendarService.create(calendar);
        var location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.getId())
            .toUri();
        return ResponseEntity.status(HttpStatus.CREATED)
            .location(location)
            .body(response);
    }

    @GetMapping("")
    @Operation(
        summary = "Get all calendars",
        description = "Retrieves a list of all calendar entries"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved list of calendars",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = CalendarResponse.class))
            )
        )
    })
    /**
     * Retrieves all calendar entries.
     *
     * @return ResponseEntity with status 200 (OK) and a list of all calendar responses
     */
    public ResponseEntity<List<CalendarResponse>> getCalendars() {
        List<CalendarResponse> response = calendarService.findAll();
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
        CalendarResponse response = calendarService.findById(id)
            .orElseThrow(() -> new CalendarNotFoundException(id));
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
        calendarService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

