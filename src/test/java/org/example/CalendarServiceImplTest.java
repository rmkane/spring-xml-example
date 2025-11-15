package org.example;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Optional;

import org.example.dto.request.CalendarRequest;
import org.example.dto.request.CalendarMetadataRequest;
import org.example.dto.response.CalendarResponse;
import org.example.dto.response.CalendarMetadataResponse;
import org.example.exception.CalendarAlreadyExistsException;
import org.example.mapper.CalendarRequestMapper;
import org.example.mapper.CalendarResponseMapper;
import org.example.model.CalendarVisibility;
import org.example.model.CalendarState;
import org.example.persistence.entity.Calendar;
import org.example.persistence.entity.CalendarMetadata;
import org.example.manager.CalendarManager;
import org.example.service.impl.CalendarServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CalendarService Unit Tests")
class CalendarServiceImplTest {
    private static final String CREATED_AT = "01/15/2025 14:30:00";
    private static final String UPDATED_AT = "01/15/2025 15:00:00";
    private static final String CREATED_BY = "Test User";
    private static final String UPDATED_BY = "Test User";
    private static final Integer COUNT = 0;

    @Mock
    private CalendarManager calendarManager;

    @Mock
    private CalendarRequestMapper calendarRequestMapper;

    @Mock
    private CalendarResponseMapper calendarResponseMapper;

    @InjectMocks
    private CalendarServiceImpl calendarService;

    private CalendarRequest testRequest;
    private Calendar testEntity;
    private CalendarResponse testResponse;

    @BeforeEach
    void setUp() {
        CalendarMetadataRequest metadataRequest = CalendarMetadataRequest.builder()
            .status(CalendarState.ACTIVE)
            .visibility(CalendarVisibility.PERSONAL)
            .createdAt(CREATED_AT)
            .createdBy(CREATED_BY)
            .updatedAt(UPDATED_AT)
            .updatedBy(UPDATED_BY)
            .count(COUNT)
            .build();
        
        testRequest = CalendarRequest.builder()
            .id("test-id-123")
            .name("Test Calendar")
            .description("Test Description")
            .metadata(metadataRequest)
            .events(new ArrayList<>())
            .build();

        CalendarMetadata calendarMetadata = new CalendarMetadata(
            CalendarState.ACTIVE,
            CalendarVisibility.PERSONAL,
            CREATED_AT,
            CREATED_BY,
            UPDATED_AT,
            UPDATED_BY,
            COUNT
        );
        testEntity = new Calendar(
            "test-id-123",
            "Test Calendar",
            "Test Description",
            calendarMetadata,
            new ArrayList<>()
        );

        CalendarMetadataResponse metadataResponse = new CalendarMetadataResponse(
            CalendarState.ACTIVE,
            CalendarVisibility.PERSONAL,
            CREATED_AT,
            CREATED_BY,
            UPDATED_AT,
            UPDATED_BY,
            COUNT
        );
        testResponse = new CalendarResponse();
        testResponse.setId("test-id-123");
        testResponse.setName("Test Calendar");
        testResponse.setDescription("Test Description");
        testResponse.setMetadata(metadataResponse);
        testResponse.setEvents(new ArrayList<>());
    }

    @Test
    @DisplayName("Should create calendar successfully when ID is provided")
    void shouldCreateCalendarWithProvidedId() {
        // Given
        when(calendarManager.findById("test-id-123")).thenReturn(Optional.empty());
        when(calendarRequestMapper.toEntity(testRequest)).thenReturn(testEntity);
        when(calendarManager.save(testEntity)).thenReturn(testEntity);
        when(calendarResponseMapper.toResponse(testEntity)).thenReturn(testResponse);

        // When
        CalendarResponse result = calendarService.create(testRequest);

        // Then
        assertNotNull(result);
        assertEquals("test-id-123", result.getId());
        assertEquals("Test Calendar", result.getName());
        verify(calendarManager).findById("test-id-123");
        verify(calendarRequestMapper).toEntity(testRequest);
        verify(calendarManager).save(testEntity);
        verify(calendarResponseMapper).toResponse(testEntity);
    }

    @Test
    @DisplayName("Should generate ID when ID is null")
    void shouldGenerateIdWhenIdIsNull() {
        // Given
        testRequest.setId(null);
        when(calendarRequestMapper.toEntity(any(CalendarRequest.class))).thenReturn(testEntity);
        when(calendarManager.save(any(Calendar.class))).thenReturn(testEntity);
        when(calendarResponseMapper.toResponse(any(Calendar.class))).thenReturn(testResponse);

        // When
        CalendarResponse result = calendarService.create(testRequest);

        // Then
        assertNotNull(result);
        assertNotNull(testRequest.getId()); // ID should be generated
        assertFalse(testRequest.getId().isEmpty());
        verify(calendarManager, never()).findById(null); // Should not check for null ID
        verify(calendarManager).save(any(Calendar.class));
    }

    @Test
    @DisplayName("Should generate ID when ID is empty")
    void shouldGenerateIdWhenIdIsEmpty() {
        // Given
        testRequest.setId("");
        when(calendarRequestMapper.toEntity(any(CalendarRequest.class))).thenReturn(testEntity);
        when(calendarManager.save(any(Calendar.class))).thenReturn(testEntity);
        when(calendarResponseMapper.toResponse(any(Calendar.class))).thenReturn(testResponse);

        // When
        CalendarResponse result = calendarService.create(testRequest);

        // Then
        assertNotNull(result);
        assertNotNull(testRequest.getId());
        assertFalse(testRequest.getId().isEmpty());
        verify(calendarManager, never()).findById(""); // Should not check for empty ID
        verify(calendarManager).save(any(Calendar.class));
    }

    @Test
    @DisplayName("Should throw exception when calendar with same ID already exists")
    void shouldThrowExceptionWhenCalendarAlreadyExists() {
        // Given
        when(calendarManager.findById("test-id-123")).thenReturn(Optional.of(testEntity));

        // When & Then
        CalendarAlreadyExistsException exception = assertThrows(
            CalendarAlreadyExistsException.class,
            () -> calendarService.create(testRequest)
        );

        assertEquals("Calendar with id test-id-123 already exists", exception.getMessage());
        verify(calendarManager).findById("test-id-123");
        verify(calendarManager, never()).save(any());
    }

    @Test
    @DisplayName("Should find calendar by ID")
    void shouldFindCalendarById() {
        // Given
        when(calendarManager.findById("test-id-123")).thenReturn(Optional.of(testEntity));
        when(calendarResponseMapper.toResponse(testEntity)).thenReturn(testResponse);

        // When
        Optional<CalendarResponse> result = calendarService.findById("test-id-123");

        // Then
        assertTrue(result.isPresent());
        assertEquals("test-id-123", result.get().getId());
        verify(calendarManager).findById("test-id-123");
        verify(calendarResponseMapper).toResponse(testEntity);
    }

    @Test
    @DisplayName("Should return empty when calendar not found")
    void shouldReturnEmptyWhenCalendarNotFound() {
        // Given
        when(calendarManager.findById("non-existent-id")).thenReturn(Optional.empty());

        // When
        Optional<CalendarResponse> result = calendarService.findById("non-existent-id");

        // Then
        assertTrue(result.isEmpty());
        verify(calendarManager).findById("non-existent-id");
        verify(calendarResponseMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Should delete calendar by ID")
    void shouldDeleteCalendarById() {
        // Given
        when(calendarManager.findById("test-id-123")).thenReturn(Optional.of(testEntity));

        // When
        calendarService.deleteById("test-id-123");

        // Then
        verify(calendarManager).findById("test-id-123");
        verify(calendarManager).deleteById("test-id-123");
    }
}

