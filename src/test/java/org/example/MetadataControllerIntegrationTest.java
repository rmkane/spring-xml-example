package org.example;

import static org.junit.jupiter.api.Assertions.*;

import org.example.dto.request.EntryRequest;
import org.example.dto.request.InfoRequest;
import org.example.dto.request.MetadataRequest;
import org.example.dto.response.MetadataResponse;
import org.example.model.MetadataState;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Metadata Controller Integration Tests")
class MetadataControllerIntegrationTest {
    private static final DateTimeFormatter DATE_CREATED_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME_CREATED_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATETIME_CREATED_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");

    private static final LocalDate CREATED_DATE = LocalDate.of(2025, 11, 13);
    private static final LocalTime CREATED_TIME = LocalTime.of(12, 0, 0);
    private static final LocalDateTime CREATED_DATETIME = LocalDateTime.of(2025, 11, 13, 12, 0, 0);

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ResourceLoader resourceLoader;

    private static final String BASE_URL = "/api/metadata";

    @Test
    @DisplayName("Should create metadata from XML and return JSON")
    void shouldCreateMetadataFromXml() throws IOException {
        // Given
        String xmlRequest = loadXmlResource("/metadata.xml");
        HttpEntity<String> request = new HttpEntity<>(xmlRequest, createXmlHeaders());

        // When
        ResponseEntity<MetadataResponse> response = restTemplate.postForEntity(
            BASE_URL,
            request,
            MetadataResponse.class
        );

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        MetadataResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("012345678-9012-3456-7890-123456789012", body.getId());
        assertEquals("Spring XML Example", body.getName());
        assertEquals("This is a test description", body.getDescription());
        assertNotNull(body.getInfo());
        assertEquals(MetadataState.ACTIVE, body.getInfo().getState());
        assertNotNull(response.getHeaders().getLocation());
        var location = response.getHeaders().getLocation();
        assertNotNull(location);
        assertTrue(location.toString().contains("/api/metadata/012345678-9012-3456-7890-123456789012"));
    }

    @Test
    @DisplayName("Should retrieve all metadata")
    void shouldFindAllMetadata() {
        // Given - create a metadata entry first
        createTestMetadata("test-all-1", "Test 1", MetadataState.ACTIVE);
        createTestMetadata("test-all-2", "Test 2", MetadataState.INACTIVE);

        // When
        ResponseEntity<MetadataResponse[]> response = restTemplate.getForEntity(
            BASE_URL,
            MetadataResponse[].class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        MetadataResponse[] body = response.getBody();
        assertNotNull(body);
        assertTrue(body.length >= 2);
    }

    @Test
    @DisplayName("Should retrieve metadata by ID")
    void shouldFindMetadataById() {
        // Given
        String testId = "test-find-by-id";
        createTestMetadata(testId, "Find By ID Test", MetadataState.ACTIVE);

        // When
        ResponseEntity<MetadataResponse> response = restTemplate.getForEntity(
            BASE_URL + "/" + testId,
            MetadataResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        MetadataResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(testId, body.getId());
        assertEquals("Find By ID Test", body.getName());
        assertNotNull(body.getInfo());
        assertEquals(MetadataState.ACTIVE, body.getInfo().getState());
    }

    @Test
    @DisplayName("Should return 404 when metadata not found")
    void shouldReturn404WhenMetadataNotFound() {
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

    @Test
    @DisplayName("Should delete metadata by ID")
    void shouldDeleteMetadataById() {
        // Given
        String testId = "test-delete";
        createTestMetadata(testId, "Delete Test", MetadataState.ACTIVE);

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

    @Test
    @DisplayName("Should return 400 when creating duplicate metadata")
    void shouldReturn400WhenCreatingDuplicateMetadata() {
        // Given
        String testId = "test-duplicate";
        createTestMetadata(testId, "First", MetadataState.ACTIVE);

        // When - try to create duplicate using the helper method
        InfoRequest info = InfoRequest.builder()
            .state(MetadataState.ACTIVE)
            .createdDate(CREATED_DATE)
            .createdTime(CREATED_TIME)
            .createdDatetime(CREATED_DATETIME)
            .build();
        MetadataRequest metadata = MetadataRequest.builder()
            .id(testId)
            .name("Duplicate")
            .description("This should fail")
            .info(info)
            .entries(new ArrayList<>())
            .build();
        String xmlRequest = createTestMetadataXml(metadata);
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
        MetadataState[] states = {MetadataState.UNKNOWN, MetadataState.ACTIVE, MetadataState.INACTIVE};

        for (int i = 0; i < states.length; i++) {
            String testId = "test-state-" + i;
            
            // When - create metadata with each state
            ResponseEntity<MetadataResponse> createResponse = createTestMetadataWithResponse(
                testId, "State Test", states[i]
            );

            // Then - verify the state was parsed correctly
            assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
            assertNotNull(createResponse.getBody());
            MetadataResponse body = createResponse.getBody();
            assertNotNull(body);
            assertNotNull(body.getInfo());
            assertEquals(states[i], body.getInfo().getState());
            
            // Also verify by fetching it
            ResponseEntity<MetadataResponse> getResponse = restTemplate.getForEntity(
                BASE_URL + "/" + testId,
                MetadataResponse.class
            );
            assertEquals(HttpStatus.OK, getResponse.getStatusCode());
            assertNotNull(getResponse.getBody());
            MetadataResponse fetchedBody = getResponse.getBody();
            assertNotNull(fetchedBody);
            assertNotNull(fetchedBody.getInfo());
            assertEquals(states[i], fetchedBody.getInfo().getState());
        }
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
     * Creates test metadata and returns the response.
     *
     * @param id the metadata ID
     * @param name the metadata name
     * @param state the metadata state
     * @return the response entity
     */
    private ResponseEntity<MetadataResponse> createTestMetadataWithResponse(String id, String name, MetadataState state) {
        InfoRequest info = InfoRequest.builder()
            .state(state)
            .createdDate(CREATED_DATE)
            .createdTime(CREATED_TIME)
            .createdDatetime(CREATED_DATETIME)
            .build();
        
        MetadataRequest metadata = MetadataRequest.builder()
            .id(id)
            .name(name)
            .description("Test description")
            .info(info)
            .entries(new ArrayList<>())
            .build();
        String xmlRequest = createTestMetadataXml(metadata);
        HttpEntity<String> request = new HttpEntity<>(xmlRequest, createXmlHeaders());

        return restTemplate.postForEntity(BASE_URL, request, MetadataResponse.class);
    }

    private String createTestMetadataXml(MetadataRequest request) {
        String description = request.getDescription() != null 
            ? request.getDescription() 
            : "Test description";
        
        String infoXml = createTestMetadataInfoXml(request.getInfo());
        String entriesXml = createTestMetadataEntriesXml(request.getEntries());
        
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <metadata id="%s">
                <name>%s</name>
                <description>%s</description>
                %s
            %s</metadata>
            """.formatted(
            request.getId() != null ? request.getId() : "",
            request.getName() != null ? request.getName() : "",
            description,
            infoXml,
            entriesXml
        );
    }

    /**
     * Creates the info section XML for a metadata request.
     *
     * @param info the info request
     * @return formatted multiline XML string for the info section
     */
    private String createTestMetadataInfoXml(InfoRequest info) {
        String stateValue = "unknown";
        String createdDateStr = "";
        String createdTimeStr = "";
        String createdDatetimeStr = "";
        
        if (info != null) {
            stateValue = Optional.ofNullable(info.getState()).map(state -> state.name().toLowerCase()).orElse("unknown");
            createdDateStr = Optional.ofNullable(info.getCreatedDate()).map(date -> date.format(DATE_CREATED_FORMATTER)).orElse("");
            createdTimeStr = Optional.ofNullable(info.getCreatedTime()).map(time -> time.format(TIME_CREATED_FORMATTER)).orElse("");
            createdDatetimeStr = Optional.ofNullable(info.getCreatedDatetime()).map(datetime -> datetime.format(DATETIME_CREATED_FORMATTER)).orElse("");
        }
        
        return """
                <info>
                    <state>%s</state>
                    <created-date>%s</created-date>
                    <created-time>%s</created-time>
                    <created-datetime>%s</created-datetime>
                </info>
            """.formatted(
            stateValue,
            createdDateStr,
            createdTimeStr,
            createdDatetimeStr
        );
    }

    /**
     * Creates the entries section XML for a metadata request.
     *
     * @param entries the list of entry requests
     * @return formatted multiline XML string for the entries section (empty string if no entries)
     */
    private String createTestMetadataEntriesXml(List<EntryRequest> entries) {
        if (entries == null || entries.isEmpty()) {
            return "";
        }
        
        StringBuilder entriesXml = new StringBuilder("        <entries>\n");
        for (EntryRequest entry : entries) {
            entriesXml.append(createTestMetadataEntryXml(entry));
        }
        entriesXml.append("        </entries>\n");
        return entriesXml.toString();
    }

    /**
     * Creates a single entry XML for a metadata request.
     *
     * @param entry the entry request
     * @return formatted multiline XML string for a single entry
     */
    private String createTestMetadataEntryXml(EntryRequest entry) {
        String typeValue = Optional.ofNullable(entry.getType())
            .map(type -> type.name().toLowerCase())
            .orElse("standard");
        String countValue = Optional.ofNullable(entry.getCount())
            .map(String::valueOf)
            .orElse("0");
        
        return """
                <entry>
                    <name>%s</name>
                    <count>%s</count>
                    <type>%s</type>
                </entry>
            """.formatted(
            entry.getName() != null ? entry.getName() : "",
            countValue,
            typeValue
        );
    }

    /**
     * Creates test metadata (convenience method that discards response).
     *
     * @param id the metadata ID
     * @param name the metadata name
     * @param state the metadata state
     */
    private void createTestMetadata(String id, String name, MetadataState state) {
        createTestMetadataWithResponse(id, name, state);
    }
}
