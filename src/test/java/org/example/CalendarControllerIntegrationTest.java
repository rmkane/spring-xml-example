package org.example;

import static org.junit.jupiter.api.Assertions.*;

import org.example.dto.request.CalendarRequest;
import org.example.dto.request.EventRequest;
import org.example.dto.request.CalendarMetadataRequest;
import org.example.dto.response.CalendarResponse;
import org.example.dto.response.PagedResponse;
import org.example.model.CalendarVisibility;
import org.example.model.CalendarState;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Calendar Controller Integration Tests")
class CalendarControllerIntegrationTest {

    private static final String CREATED_AT = "11/13/2025 12:00:00";
    private static final String UPDATED_AT = "11/13/2025 14:30:00";
    private static final String CREATED_BY = "Test User";
    private static final String UPDATED_BY = "Test User";
    private static final Integer COUNT = 0;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ResourceLoader resourceLoader;

    private static final String BASE_URL = "/api/calendars";
    
    // Track calendar IDs created during tests for cleanup
    private final Set<String> createdCalendarIds = new HashSet<>();

    // =============================================================================
    // CREATE Tests
    // =============================================================================

    @Test
    @DisplayName("Should create calendar from XML and return JSON")
    void shouldCreateCalendarFromXml() throws IOException {
        // Given
        String xmlRequest = loadXmlResource("/calendars/20dbf44a-b88b-4742-a0b0-1d6c7dece68d.xml");
        HttpEntity<String> request = new HttpEntity<>(xmlRequest, createXmlHeaders());

        // When
        ResponseEntity<CalendarResponse> response = restTemplate.postForEntity(
            BASE_URL,
            request,
            CalendarResponse.class
        );
        
        // Track for cleanup
        CalendarResponse responseBody = response.getBody();
        if (responseBody != null && responseBody.getId() != null) {
            createdCalendarIds.add(responseBody.getId());
        }

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        CalendarResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("20dbf44a-b88b-4742-a0b0-1d6c7dece68d", body.getId());
        assertEquals("Work Calendar", body.getName());
        assertEquals("Work schedule and meetings", body.getDescription());
        assertNotNull(body.getMetadata());
        assertEquals(CalendarState.ACTIVE, body.getMetadata().getStatus());
        assertNotNull(response.getHeaders().getLocation());
        var location = response.getHeaders().getLocation();
        assertNotNull(location);
        assertTrue(location.toString().contains("/api/calendars/20dbf44a-b88b-4742-a0b0-1d6c7dece68d"));
    }

    @Test
    @DisplayName("Should return 400 when creating duplicate calendar")
    void shouldReturn400WhenCreatingDuplicateCalendar() {
        // Given
        String testId = "550e8400-e29b-41d4-a716-446655440000";
        createTestCalendar(testId, "First", CalendarState.ACTIVE);

        // When - try to create duplicate using the helper method
        CalendarMetadataRequest metadataRequest = CalendarMetadataRequest.builder()
            .status(CalendarState.ACTIVE)
            .createdAt(CREATED_AT)
            .createdBy(CREATED_BY)
            .count(COUNT)
            .build();
        CalendarRequest calendar = CalendarRequest.builder()
            .id(testId)
            .name("Duplicate")
            .description("This should fail")
            .metadata(metadataRequest)
            .events(new ArrayList<>())
            .build();
        String xmlRequest = createTestCalendarXml(calendar);
        HttpEntity<String> request = new HttpEntity<>(xmlRequest, createXmlHeaders());
        ResponseEntity<String> response = restTemplate.postForEntity(BASE_URL, request, String.class);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        String body = response.getBody();
        assertNotNull(body);
        assertTrue(body.contains("already exists") || body.contains(testId));
    }

    @Test
    @DisplayName("Should parse enum state from XML correctly")
    void shouldParseEnumStateFromXml() {
        // Given - test all enum values
        CalendarState[] states = {CalendarState.UNKNOWN, CalendarState.ACTIVE, CalendarState.INACTIVE};
        String[] testIds = {
            "550e8400-e29b-41d4-a716-446655440001",
            "550e8400-e29b-41d4-a716-446655440002",
            "550e8400-e29b-41d4-a716-446655440003"
        };

        for (int i = 0; i < states.length; i++) {
            String testId = testIds[i];
            
            // When - create calendar with each state
            ResponseEntity<CalendarResponse> createResponse = createTestCalendarWithResponse(
                testId, "State Test", states[i]
            );

            // Then - verify the state was parsed correctly
            assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
            assertNotNull(createResponse.getBody());
            CalendarResponse body = createResponse.getBody();
            assertNotNull(body);
            assertNotNull(body.getMetadata());
            assertEquals(states[i], body.getMetadata().getStatus());
            
            // Also verify by fetching it
            ResponseEntity<CalendarResponse> getResponse = restTemplate.getForEntity(
                BASE_URL + "/" + testId,
                CalendarResponse.class
            );
            assertEquals(HttpStatus.OK, getResponse.getStatusCode());
            assertNotNull(getResponse.getBody());
            CalendarResponse fetchedBody = getResponse.getBody();
            assertNotNull(fetchedBody);
            assertNotNull(fetchedBody.getMetadata());
            assertEquals(states[i], fetchedBody.getMetadata().getStatus());
        }
    }

    // =============================================================================
    // READ Tests
    // =============================================================================

    @Test
    @DisplayName("Should retrieve all calendars")
    void shouldFindAllCalendars() {
        // Given - create calendars first
        createTestCalendar("550e8400-e29b-41d4-a716-446655440010", "Test 1", CalendarState.ACTIVE);
        createTestCalendar("550e8400-e29b-41d4-a716-446655440011", "Test 2", CalendarState.INACTIVE);

        // When
        ResponseEntity<PagedResponse<CalendarResponse>> response = restTemplate.exchange(
            BASE_URL,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<PagedResponse<CalendarResponse>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        PagedResponse<CalendarResponse> body = response.getBody();
        assertNotNull(body);
        assertNotNull(body.getItems());
        assertTrue(body.getItems().size() >= 2);
        assertTrue(body.getTotalElements() >= 2);
        assertEquals(0, body.getPage());
        assertEquals(10, body.getSize());
    }

    @Test
    @DisplayName("Should retrieve calendar by ID")
    void shouldFindCalendarById() {
        // Given
        String testId = "550e8400-e29b-41d4-a716-446655440020";
        createTestCalendar(testId, "Find By ID Test", CalendarState.ACTIVE);

        // When
        ResponseEntity<CalendarResponse> response = restTemplate.getForEntity(
            BASE_URL + "/" + testId,
            CalendarResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        CalendarResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(testId, body.getId());
        assertEquals("Find By ID Test", body.getName());
        assertNotNull(body.getMetadata());
        assertEquals(CalendarState.ACTIVE, body.getMetadata().getStatus());
    }

    @Test
    @DisplayName("Should return 404 when calendar not found")
    void shouldReturn404WhenCalendarNotFound() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
            BASE_URL + "/non-existent-id",
            String.class
        );

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        String body = response.getBody();
        assertNotNull(body);
        assertTrue(body.contains("not found") || body.contains("non-existent-id"));
    }

    // =============================================================================
    // DELETE Tests
    // =============================================================================

    @Test
    @DisplayName("Should delete calendar by ID")
    void shouldDeleteCalendarById() {
        // Given
        String testId = "550e8400-e29b-41d4-a716-446655440030";
        createTestCalendar(testId, "Delete Test", CalendarState.ACTIVE);

        // When
        ResponseEntity<Void> response = restTemplate.exchange(
            BASE_URL + "/" + testId,
            HttpMethod.DELETE,
            null,
            Void.class
        );

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Verify it's deleted
        ResponseEntity<String> getResponse = restTemplate.getForEntity(
            BASE_URL + "/" + testId,
            String.class
        );
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }

    /**
     * Loads an XML file from the classpath resources.
     *
     * @param resourcePath the path to the resource file (e.g., "test-metadata.xml")
     * @return the file contents as a String
     * @throws IOException if the resource cannot be loaded
     */
    private String loadXmlResource(String resourcePath) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:" + resourcePath);
        @SuppressWarnings("null")
        String content = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        return content;
    }

    /**
     * Creates default HTTP headers for XML requests.
     *
     * @return HttpHeaders with XML content type
     */
    private HttpHeaders createXmlHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        return headers;
    }

    /**
     * Creates test calendar and returns the response.
     *
     * @param id the calendar ID
     * @param name the calendar name
     * @param state the calendar state
     * @return the response entity
     */
    private ResponseEntity<CalendarResponse> createTestCalendarWithResponse(String id, String name, CalendarState state) {
        CalendarMetadataRequest metadataRequest = CalendarMetadataRequest.builder()
            .status(state)
            .visibility(CalendarVisibility.PERSONAL)
            .createdAt(CREATED_AT)
            .createdBy(CREATED_BY)
            .updatedAt(UPDATED_AT)
            .updatedBy(UPDATED_BY)
            .count(COUNT)
            .build();
        
        CalendarRequest calendar = CalendarRequest.builder()
            .id(id)
            .name(name)
            .description("Test description")
            .metadata(metadataRequest)
            .events(new ArrayList<>())
            .build();
        String xmlRequest = createTestCalendarXml(calendar);
        HttpEntity<String> request = new HttpEntity<>(xmlRequest, createXmlHeaders());

        ResponseEntity<CalendarResponse> response = restTemplate.postForEntity(BASE_URL, request, CalendarResponse.class);
        
        // Track for cleanup
        CalendarResponse responseBody = response.getBody();
        if (responseBody != null && responseBody.getId() != null) {
            createdCalendarIds.add(responseBody.getId());
        }
        
        return response;
    }

    private String createTestCalendarXml(CalendarRequest request) {
        String description = request.getDescription() != null 
            ? request.getDescription() 
            : "Test description";
        
        String metadataXml = createTestMetadataXml(request.getMetadata());
        String eventsXml = createTestMetadataEventsXml(request.getEvents());
        
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <calendar id="%s">
                <name>%s</name>
                <description>%s</description>
                %s
            %s</calendar>
            """.formatted(
            request.getId() != null ? request.getId() : "",
            request.getName() != null ? request.getName() : "",
            description,
            metadataXml,
            eventsXml
        );
    }

    /**
     * Creates the metadata section XML for a calendar request.
     *
     * @param metadata the metadata request
     * @return formatted multiline XML string for the metadata section
     */
    private String createTestMetadataXml(CalendarMetadataRequest metadata) {
        if (metadata == null) {
            return """
                    <metadata>
                        <status>unknown</status>
                        <visibility>personal</visibility>
                        <created-at></created-at>
                        <created-by></created-by>
                        <updated-at></updated-at>
                        <updated-by></updated-by>
                        <count>0</count>
                    </metadata>
                """;
        }
        
        String status = formatEnum(metadata.getStatus(), "unknown");
        String visibility = formatEnum(metadata.getVisibility(), "personal");
        String createdAt = wrapWithXmlTag(metadata.getCreatedAt(), "created-at");
        String createdBy = wrapWithXmlTag(metadata.getCreatedBy(), "created-by");
        String updatedAt = wrapWithXmlTag(metadata.getUpdatedAt(), "updated-at");
        String updatedBy = wrapWithXmlTag(metadata.getUpdatedBy(), "updated-by");
        String count = wrapWithXmlTag(formatInteger(metadata.getCount()), "count");
        
        return """
                <metadata>
                    <status>%s</status>
                    <visibility>%s</visibility>
                    %s%s%s%s%s</metadata>
            """.formatted(
                status,
            visibility,
            createdAt,
            createdBy,
            updatedAt,
            updatedBy,
            count
        );
    }

    /**
     * Creates the events section XML for a calendar request.
     *
     * @param events the list of event requests
     * @return formatted multiline XML string for the events section (empty string if no events)
     */
    private String createTestMetadataEventsXml(List<EventRequest> events) {
        if (events == null || events.isEmpty()) {
            return "";
        }
        
        StringBuilder eventsXml = new StringBuilder();
        for (EventRequest event : events) {
            eventsXml.append(createTestMetadataEventXml(event));
        }
        return eventsXml.toString();
    }

    /**
     * Creates a single event XML for a calendar request.
     *
     * @param event the event request
     * @return formatted multiline XML string for a single event
     */
    private String createTestMetadataEventXml(EventRequest event) {
        String name = Optional.ofNullable(event.getName()).orElse("");
        String description = Optional.ofNullable(event.getDescription()).orElse("");
        String idAttr = Optional.ofNullable(event.getId()).map(s -> String.format(" id=\"%s\"", s)).orElse("");
        String typeValue = Optional.ofNullable(event.getType())
            .map(type -> type.name().toLowerCase())
            .orElse("other");
        String disabledValue = formatBoolean(event.getDisabled());
        String allDayValue = formatBoolean(event.getAllDay());
        String startDateTime = formatDateTime(event.getStartDateTime(), "MM/dd/yyyy HH:mm:ss");
        String endDateTime = formatDateTime(event.getEndDateTime(), "MM/dd/yyyy HH:mm:ss");
        String location = wrapWithXmlTag(event.getLocation(), "location");
        String createdAt = wrapWithXmlTag(event.getCreatedAt(), "created-at");
        String createdBy = wrapWithXmlTag(event.getCreatedBy(), "created-by");
        String updatedAt = wrapWithXmlTag(event.getUpdatedAt(), "updated-at");
        String updatedBy = wrapWithXmlTag(event.getUpdatedBy(), "updated-by");
        
        return """
                <event%s>
                    <name>%s</name>
                    <description>%s</description>
                    <type>%s</type>
                    <disabled>%s</disabled>
                    <all-day>%s</all-day>
                    <start-datetime>%s</start-datetime>
                    <end-datetime>%s</end-datetime>
                    %s%s%s%s</event>
            """.formatted(
            idAttr,
            name,
            description,
            typeValue,
            disabledValue,
            allDayValue,
            startDateTime,
            endDateTime,
            location,
            createdAt,
            createdBy,
            updatedAt,
            updatedBy
        );
    }

    /**
     * Wraps a value in an XML tag with proper indentation and newline.
     *
     * @param value the value to wrap (null-safe)
     * @param tag the XML tag name
     * @return formatted XML element or empty string if value is null
     */
    private String wrapWithXmlTag(String value, String tag) {
        return Optional.ofNullable(value)
            .filter(s -> !s.isEmpty())
            .map(s -> String.format("                    <%s>%s</%s>\n", tag, s, tag))
            .orElse("");
    }

    /**
     * Formats a LocalDateTime using the specified pattern.
     *
     * @param dateTime the date-time to format (null-safe)
     * @param pattern the date-time pattern
     * @return formatted string or empty string if dateTime is null
     */
    private String formatDateTime(LocalDateTime dateTime, String pattern) {
        return Optional.ofNullable(dateTime)
            .map(dt -> dt.format(DateTimeFormatter.ofPattern(pattern)))
            .orElse("");
    }

    /**
     * Formats a Boolean as a string.
     *
     * @param value the boolean to format (null-safe)
     * @return "true", "false", or "false" if null
     */
    private String formatBoolean(Boolean value) {
        return Optional.ofNullable(value)
            .map(v -> v ? "true" : "false")
            .orElse("false");
    }

    /**
     * Formats an Integer as a string.
     *
     * @param value the integer to format (null-safe)
     * @return formatted string or "0" if null
     */
    private String formatInteger(Integer value) {
        return Optional.ofNullable(value)
            .map(String::valueOf)
            .orElse("0");
    }

    /**
     * Formats an enum as a lowercase string.
     *
     * @param enumValue the enum to format (null-safe)
     * @param defaultValue the default value if enum is null
     * @return lowercase enum name or default value
     */
    private String formatEnum(Enum<?> enumValue, String defaultValue) {
        return Optional.ofNullable(enumValue)
            .map(e -> e.name().toLowerCase())
            .orElse(defaultValue);
    }

    /**
     * Creates test calendar (convenience method that discards response).
     * If calendar already exists, it's fine - we'll use the existing one.
     *
     * @param id the calendar ID
     * @param name the calendar name
     * @param state the calendar state
     */
    @SuppressWarnings("unused")
    private void createTestCalendar(String id, String name, CalendarState state) {
        createTestCalendarWithResponse(id, name, state);
    }
    
    /**
     * Cleans up calendars created during tests.
     * Runs after each test to ensure test isolation.
     */
    @AfterEach
    void cleanup() {
        for (String calendarId : createdCalendarIds) {
            try {
                restTemplate.delete(BASE_URL + "/" + calendarId);
            } catch (Exception e) {
                // Ignore cleanup errors - calendar might already be deleted
            }
        }
        createdCalendarIds.clear();
    }
}
