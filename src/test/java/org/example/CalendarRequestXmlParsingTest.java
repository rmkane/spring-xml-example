package org.example;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.example.dto.request.CalendarRequest;
import org.example.dto.request.EventRequest;
import org.example.dto.request.InfoRequest;
import org.example.model.CalendarVisibility;
import org.example.model.EventType;
import org.example.model.CalendarState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@DisplayName("CalendarRequest XML Parsing Tests")
class CalendarRequestXmlParsingTest {

    private final XmlMapper xmlMapper;

    public CalendarRequestXmlParsingTest() {
        xmlMapper = new XmlMapper();
        xmlMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("Should parse XML test resource to CalendarRequest with all fields")
    void shouldParseXmlTestResourceToCalendarRequest() throws IOException {
        // Given
        String xmlContent = loadXmlResource("/calendars/20dbf44a-b88b-4742-a0b0-1d6c7dece68d.xml");

        // When
        CalendarRequest request = xmlMapper.readValue(xmlContent, CalendarRequest.class);

        // Then
        assertNotNull(request);
        assertEquals("20dbf44a-b88b-4742-a0b0-1d6c7dece68d", request.getId());
        assertEquals("Work Calendar", request.getName());
        assertEquals("Work schedule and meetings", request.getDescription());

        // Verify metadata section
        assertNotNull(request.getMetadata());
        InfoRequest metadata = request.getMetadata();
        assertEquals(CalendarState.ACTIVE, metadata.getStatus());
        assertEquals(CalendarVisibility.SHARED, metadata.getVisibility());
        assertEquals("11/13/2025 12:00:00", metadata.getCreatedAt());
        assertEquals("John Doe", metadata.getCreatedBy());
        assertEquals("11/13/2025 14:30:00", metadata.getUpdatedAt());
        assertEquals("Jane Doe", metadata.getUpdatedBy());
        assertEquals(5, metadata.getCount());

        // Verify events (unwrapped)
        assertNotNull(request.getEvents());
        assertEquals(5, request.getEvents().size());

        EventRequest event1 = request.getEvents().get(0);
        assertEquals("a1b2c3d4-e5f6-7890-abcd-111111111111", event1.getId());
        assertEquals("Team Standup", event1.getName());
        assertEquals("Daily team standup meeting", event1.getDescription());
        assertEquals(EventType.MEETING, event1.getType());
        assertEquals(false, event1.getDisabled());
        assertEquals(false, event1.getAllDay());
        assertEquals(LocalDateTime.of(2025, 11, 14, 9, 0, 0), event1.getStartDateTime());
        assertEquals(LocalDateTime.of(2025, 11, 14, 9, 30, 0), event1.getEndDateTime());
        assertEquals("Zoom", event1.getLocation());
        assertEquals("11/10/2025 10:00:00", event1.getCreatedAt());
        assertEquals("John Doe", event1.getCreatedBy());
        assertEquals("11/13/2025 14:30:00", event1.getUpdatedAt());
        assertEquals("Jane Doe", event1.getUpdatedBy());

        EventRequest event2 = request.getEvents().get(1);
        assertEquals("a1b2c3d4-e5f6-7890-abcd-222222222222", event2.getId());
        assertEquals("Thanksgiving", event2.getName());
        assertEquals("Thanksgiving holiday", event2.getDescription());
        assertEquals(EventType.HOLIDAY, event2.getType());
        assertEquals(false, event2.getDisabled());
        assertEquals(true, event2.getAllDay());
        assertEquals(LocalDateTime.of(2025, 11, 27, 0, 0, 0), event2.getStartDateTime());
        assertEquals(LocalDateTime.of(2025, 11, 27, 23, 59, 59), event2.getEndDateTime());
        assertNull(event2.getLocation()); // All-day holiday doesn't have location
        assertEquals("11/01/2025 08:00:00", event2.getCreatedAt());
        assertEquals("John Doe", event2.getCreatedBy());
        assertEquals("11/13/2025 14:30:00", event2.getUpdatedAt());
        assertEquals("John Doe", event2.getUpdatedBy());

        EventRequest event3 = request.getEvents().get(2);
        assertEquals("a1b2c3d4-e5f6-7890-abcd-333333333333", event3.getId());
        assertEquals("Doctor Appointment", event3.getName());
        assertEquals("Annual checkup", event3.getDescription());
        assertEquals(EventType.APPOINTMENT, event3.getType());
        assertEquals(false, event3.getDisabled());
        assertEquals(false, event3.getAllDay());
        assertEquals(LocalDateTime.of(2025, 12, 5, 14, 0, 0), event3.getStartDateTime());
        assertEquals(LocalDateTime.of(2025, 12, 5, 15, 0, 0), event3.getEndDateTime());
        assertEquals("Medical Center", event3.getLocation());
        assertEquals("11/15/2025 09:00:00", event3.getCreatedAt());
        assertEquals("John Doe", event3.getCreatedBy());
        assertEquals("11/15/2025 09:00:00", event3.getUpdatedAt());
        assertEquals("John Doe", event3.getUpdatedBy());

        EventRequest event4 = request.getEvents().get(3);
        assertEquals("a1b2c3d4-e5f6-7890-abcd-444444444444", event4.getId());
        assertEquals("Project Deadline Reminder", event4.getName());
        assertEquals("Submit final report", event4.getDescription());
        assertEquals(EventType.REMINDER, event4.getType());
        assertEquals(false, event4.getDisabled());
        assertEquals(false, event4.getAllDay());
        assertEquals(LocalDateTime.of(2025, 12, 15, 17, 0, 0), event4.getStartDateTime());
        assertEquals(LocalDateTime.of(2025, 12, 15, 17, 0, 0), event4.getEndDateTime());
        assertNull(event4.getLocation()); // Reminder doesn't have location
        assertEquals("11/20/2025 11:00:00", event4.getCreatedAt());
        assertEquals("Jane Doe", event4.getCreatedBy());
        assertEquals("11/20/2025 11:00:00", event4.getUpdatedAt());
        assertEquals("Jane Doe", event4.getUpdatedBy());

        EventRequest event5 = request.getEvents().get(4);
        assertEquals("a1b2c3d4-e5f6-7890-abcd-555555555555", event5.getId());
        assertEquals("Holiday Break", event5.getName());
        assertEquals("Year-end holidays", event5.getDescription());
        assertEquals(EventType.HOLIDAY, event5.getType());
        assertEquals(false, event5.getDisabled());
        assertEquals(true, event5.getAllDay());
        assertEquals(LocalDateTime.of(2025, 12, 25, 0, 0, 0), event5.getStartDateTime());
        assertEquals(LocalDateTime.of(2026, 1, 1, 23, 59, 59), event5.getEndDateTime());
        assertNull(event5.getLocation()); // All-day holiday doesn't have location
        assertEquals("11/01/2025 08:00:00", event5.getCreatedAt());
        assertEquals("John Doe", event5.getCreatedBy());
        assertEquals("11/13/2025 14:30:00", event5.getUpdatedAt());
        assertEquals("John Doe", event5.getUpdatedBy());
    }

    @Test
    @DisplayName("Should parse XML with unwrapped event elements correctly")
    void shouldParseUnwrappedEventElements() throws IOException {
        // Given
        String xmlContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <calendar id="test-id">
                <name>Test</name>
                <description>Test Description</description>
                <metadata>
                    <status>inactive</status>
                    <visibility>personal</visibility>
                    <created-at>01/01/2025 00:00:00</created-at>
                    <created-by>Test User</created-by>
                    <updated-at>01/01/2025 00:00:00</updated-at>
                    <updated-by>Test User</updated-by>
                    <count>3</count>
                </metadata>
                <event id="c1b2c3d4-e5f6-7890-abcd-111111111111">
                    <name>First Event</name>
                    <description>First event description</description>
                    <type>meeting</type>
                    <disabled>false</disabled>
                    <all-day>false</all-day>
                    <start-datetime>01/01/2025 10:00:00</start-datetime>
                    <end-datetime>01/01/2025 11:00:00</end-datetime>
                    <created-at>01/01/2025 09:00:00</created-at>
                    <created-by>Test User</created-by>
                    <updated-at>01/01/2025 00:00:00</updated-at>
                    <updated-by>Test User</updated-by>
                </event>
                <event id="c1b2c3d4-e5f6-7890-abcd-222222222222">
                    <name>Second Event</name>
                    <description>Second event description</description>
                    <type>appointment</type>
                    <disabled>false</disabled>
                    <all-day>true</all-day>
                    <start-datetime>01/02/2025 00:00:00</start-datetime>
                    <end-datetime>01/02/2025 23:59:59</end-datetime>
                    <created-at>01/01/2025 09:00:00</created-at>
                    <created-by>Test User</created-by>
                    <updated-at>01/01/2025 00:00:00</updated-at>
                    <updated-by>Test User</updated-by>
                </event>
                <event id="c1b2c3d4-e5f6-7890-abcd-333333333333">
                    <name>Third Event</name>
                    <description>Third event description</description>
                    <type>reminder</type>
                    <disabled>false</disabled>
                    <all-day>false</all-day>
                    <start-datetime>01/03/2025 15:00:00</start-datetime>
                    <end-datetime>01/03/2025 15:00:00</end-datetime>
                    <created-at>01/01/2025 09:00:00</created-at>
                    <created-by>Test User</created-by>
                    <updated-at>01/01/2025 00:00:00</updated-at>
                    <updated-by>Test User</updated-by>
                </event>
            </calendar>
            """;

        // When
        CalendarRequest request = xmlMapper.readValue(xmlContent, CalendarRequest.class);

        // Then
        assertNotNull(request);
        assertNotNull(request.getEvents());
        assertEquals(3, request.getEvents().size());
        assertEquals("First Event", request.getEvents().get(0).getName());
        assertEquals("Second Event", request.getEvents().get(1).getName());
        assertEquals("Third Event", request.getEvents().get(2).getName());
    }

    @Test
    @DisplayName("Should parse XML with no events")
    void shouldParseXmlWithNoEvents() throws IOException {
        // Given
        String xmlContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <calendar id="test-id">
                <name>Test</name>
                <description>Test Description</description>
                <metadata>
                    <status>unknown</status>
                </metadata>
            </calendar>
            """;

        // When
        CalendarRequest request = xmlMapper.readValue(xmlContent, CalendarRequest.class);

        // Then
        assertNotNull(request);
        // When no events are present, Jackson may return null or empty list
        if (request.getEvents() != null) {
            assertTrue(request.getEvents().isEmpty());
        }
    }

    @Test
    @DisplayName("Should parse XML with single event")
    void shouldParseXmlWithSingleEvent() throws IOException {
        // Given
        String xmlContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <calendar id="test-id">
                <name>Test</name>
                <description>Test Description</description>
                <metadata>
                    <status>active</status>
                    <visibility>personal</visibility>
                </metadata>
                <event id="evt_single">
                    <name>Single Event</name>
                    <description>Single event description</description>
                    <type>meeting</type>
                    <disabled>false</disabled>
                    <all-day>true</all-day>
                    <start-datetime>01/15/2025 00:00:00</start-datetime>
                    <end-datetime>01/15/2025 23:59:59</end-datetime>
                    <created-at>01/01/2025 09:00:00</created-at>
                    <created-by>Test User</created-by>
                    <updated-at>01/01/2025 00:00:00</updated-at>
                    <updated-by>Test User</updated-by>
                </event>
            </calendar>
            """;

        // When
        CalendarRequest request = xmlMapper.readValue(xmlContent, CalendarRequest.class);

        // Then
        assertNotNull(request);
        assertNotNull(request.getEvents());
        assertEquals(1, request.getEvents().size());
        EventRequest event = request.getEvents().get(0);
        assertEquals("Single Event", event.getName());
        assertEquals(EventType.MEETING, event.getType());
        assertEquals(false, event.getDisabled());
        assertEquals(true, event.getAllDay());
    }

    @Test
    @DisplayName("Should parse all enum states correctly")
    void shouldParseAllEnumStates() throws IOException {
        // Given
        CalendarState[] states = {CalendarState.UNKNOWN, CalendarState.ACTIVE, CalendarState.INACTIVE};

        for (CalendarState state : states) {
            String xmlContent = """
                <?xml version="1.0" encoding="UTF-8"?>
                <calendar id="test-%s">
                    <name>Test</name>
                    <description>Test Description</description>
                    <metadata>
                        <status>%s</status>
                    </metadata>
                </calendar>
                """.formatted(state.name().toLowerCase(), state.name().toLowerCase());

            // When
            CalendarRequest request = xmlMapper.readValue(xmlContent, CalendarRequest.class);

            // Then
            assertNotNull(request);
            assertNotNull(request.getMetadata());
            assertEquals(state, request.getMetadata().getStatus(), 
                "Failed to parse state: " + state.name().toLowerCase());
        }
    }

    @Test
    @DisplayName("Should parse all event types correctly")
    void shouldParseAllEventTypes() throws IOException {
        // Given
        EventType[] types = {EventType.HOLIDAY, EventType.MEETING, EventType.APPOINTMENT, EventType.REMINDER, EventType.OTHER};

        for (EventType type : types) {
            String xmlContent = """
                <?xml version="1.0" encoding="UTF-8"?>
                <calendar id="test-%s">
                    <name>Test</name>
                    <description>Test Description</description>
                    <metadata>
                        <status>active</status>
                        <visibility>personal</visibility>
                    </metadata>
                    <event id="evt_test">
                        <name>Test Event</name>
                        <description>Test event description</description>
                        <type>%s</type>
                        <disabled>false</disabled>
                        <all-day>true</all-day>
                        <start-datetime>01/01/2025 00:00:00</start-datetime>
                        <end-datetime>01/01/2025 23:59:59</end-datetime>
                        <created-at>01/01/2025 09:00:00</created-at>
                        <created-by>Test User</created-by>
                        <updated-at>01/01/2025 00:00:00</updated-at>
                        <updated-by>Test User</updated-by>
                    </event>
                </calendar>
                """.formatted(type.name().toLowerCase(), type.name().toLowerCase());

            // When
            CalendarRequest request = xmlMapper.readValue(xmlContent, CalendarRequest.class);

            // Then
            assertNotNull(request);
            assertNotNull(request.getEvents());
            assertEquals(1, request.getEvents().size());
            assertEquals(type, request.getEvents().get(0).getType(),
                "Failed to parse event type: " + type.name().toLowerCase());
        }
    }

    /**
     * Loads an XML file from the classpath resources.
     *
     * @param resourcePath the path to the resource file (e.g., "/calendars/20dbf44a-b88b-4742-a0b0-1d6c7dece68d.xml")
     * @return the file contents as a String
     * @throws IOException if the resource cannot be loaded
     */
    private String loadXmlResource(String resourcePath) throws IOException {
        assertNotNull(resourcePath, "Resource path cannot be null");
        ClassPathResource resource = new ClassPathResource(resourcePath);
        try (InputStream inputStream = resource.getInputStream()) {
            @SuppressWarnings("null") // StandardCharsets.UTF_8 is a constant, never null
            String content = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            assertNotNull(content, "Resource content cannot be null");
            return content;
        }
    }
}

