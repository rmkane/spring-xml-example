package org.example;

import static org.junit.jupiter.api.Assertions.*;

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
        assertEquals(MetadataState.ACTIVE, body.getState());
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
        assertEquals(MetadataState.ACTIVE, body.getState());
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
        MetadataRequest metadata = MetadataRequest.builder()
            .id(testId)
            .name("Duplicate")
            .description("This should fail")
            .state(MetadataState.ACTIVE)
            .createdDate(CREATED_DATE)
            .createdTime(CREATED_TIME)
            .createdDatetime(CREATED_DATETIME)
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
            assertEquals(states[i], body.getState());
            
            // Also verify by fetching it
            ResponseEntity<MetadataResponse> getResponse = restTemplate.getForEntity(
                BASE_URL + "/" + testId,
                MetadataResponse.class
            );
            assertEquals(HttpStatus.OK, getResponse.getStatusCode());
            assertNotNull(getResponse.getBody());
            MetadataResponse fetchedBody = getResponse.getBody();
            assertNotNull(fetchedBody);
            assertEquals(states[i], fetchedBody.getState());
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
        MetadataRequest metadata = MetadataRequest.builder()
            .id(id)
            .name(name)
            .description("Test description")
            .state(state)
            .createdDate(CREATED_DATE)
            .createdTime(CREATED_TIME)
            .createdDatetime(CREATED_DATETIME)
            .build();
        String xmlRequest = createTestMetadataXml(metadata);
        HttpEntity<String> request = new HttpEntity<>(xmlRequest, createXmlHeaders());

        return restTemplate.postForEntity(BASE_URL, request, MetadataResponse.class);
    }

    private String createTestMetadataXml(MetadataRequest request) {
        String stateValue = request.getState() != null 
            ? request.getState().name().toLowerCase() 
            : "unknown";
        String description = request.getDescription() != null 
            ? request.getDescription() 
            : "Test description";
        
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <metadata id="%s">
                <name>%s</name>
                <description>%s</description>
                <state>%s</state>
                <created-date>%s</created-date>
                <created-time>%s</created-time>
                <created-datetime>%s</created-datetime>
            </metadata>
            """.formatted(
            request.getId() != null ? request.getId() : "",
            request.getName() != null ? request.getName() : "",
            description,
            stateValue,
            request.getCreatedDate() != null ? request.getCreatedDate().format(DATE_CREATED_FORMATTER) : "",
            request.getCreatedTime() != null ? request.getCreatedTime().format(TIME_CREATED_FORMATTER) : "",
            request.getCreatedDatetime() != null ? request.getCreatedDatetime().format(DATETIME_CREATED_FORMATTER) : ""
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
