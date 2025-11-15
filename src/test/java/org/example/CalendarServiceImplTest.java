package org.example;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Optional;

import org.example.dto.request.CalendarRequest;
import org.example.dto.request.InfoRequest;
import org.example.dto.response.CalendarResponse;
import org.example.dto.response.InfoResponse;
import org.example.exception.CalendarAlreadyExistsException;
import org.example.mapper.CalendarRequestMapper;
import org.example.mapper.CalendarResponseMapper;
import org.example.model.CalendarVisibility;
import org.example.model.CalendarState;
import org.example.persistence.entity.CalendarEntity;
import org.example.persistence.entity.InfoEntity;
import org.example.persistence.repository.CalendarRepository;
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
    private CalendarRepository calendarRepository;

    @Mock
    private CalendarRequestMapper calendarRequestMapper;

    @Mock
    private CalendarResponseMapper calendarResponseMapper;

    @InjectMocks
    private CalendarServiceImpl calendarService;

    private CalendarRequest testRequest;
    private CalendarEntity testEntity;
    private CalendarResponse testResponse;

    @BeforeEach
    void setUp() {
        InfoRequest infoRequest = InfoRequest.builder()
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
            .metadata(infoRequest)
            .events(new ArrayList<>())
            .build();

        InfoEntity infoEntity = new InfoEntity(
            CalendarState.ACTIVE,
            CalendarVisibility.PERSONAL,
            CREATED_AT,
            CREATED_BY,
            UPDATED_AT,
            UPDATED_BY,
            COUNT
        );
        testEntity = new CalendarEntity(
            "test-id-123",
            "Test Calendar",
            "Test Description",
            infoEntity,
            new ArrayList<>()
        );

        InfoResponse infoResponse = new InfoResponse(
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
        testResponse.setMetadata(infoResponse);
        testResponse.setEvents(new ArrayList<>());
    }

    @Test
    @DisplayName("Should create calendar successfully when ID is provided")
    void shouldCreateCalendarWithProvidedId() {
        // Given
        when(calendarRepository.findById("test-id-123")).thenReturn(Optional.empty());
        when(calendarRequestMapper.toEntity(testRequest)).thenReturn(testEntity);
        when(calendarRepository.save(testEntity)).thenReturn(testEntity);
        when(calendarResponseMapper.toResponse(testEntity)).thenReturn(testResponse);

        // When
        CalendarResponse result = calendarService.create(testRequest);

        // Then
        assertNotNull(result);
        assertEquals("test-id-123", result.getId());
        assertEquals("Test Calendar", result.getName());
        verify(calendarRepository).findById("test-id-123");
        verify(calendarRequestMapper).toEntity(testRequest);
        verify(calendarRepository).save(testEntity);
        verify(calendarResponseMapper).toResponse(testEntity);
    }

    @Test
    @DisplayName("Should generate ID when ID is null")
    void shouldGenerateIdWhenIdIsNull() {
        // Given
        testRequest.setId(null);
        when(calendarRepository.findById(null)).thenReturn(Optional.empty());
        when(calendarRequestMapper.toEntity(any(CalendarRequest.class))).thenReturn(testEntity);
        when(calendarRepository.save(any(CalendarEntity.class))).thenReturn(testEntity);
        when(calendarResponseMapper.toResponse(any(CalendarEntity.class))).thenReturn(testResponse);

        // When
        CalendarResponse result = calendarService.create(testRequest);

        // Then
        assertNotNull(result);
        assertNotNull(testRequest.getId()); // ID should be generated
        assertFalse(testRequest.getId().isEmpty());
        verify(calendarRepository).findById(null);
        verify(calendarRepository).save(any(CalendarEntity.class));
    }

    @Test
    @DisplayName("Should generate ID when ID is empty")
    void shouldGenerateIdWhenIdIsEmpty() {
        // Given
        testRequest.setId("");
        when(calendarRepository.findById("")).thenReturn(Optional.empty());
        when(calendarRequestMapper.toEntity(any(CalendarRequest.class))).thenReturn(testEntity);
        when(calendarRepository.save(any(CalendarEntity.class))).thenReturn(testEntity);
        when(calendarResponseMapper.toResponse(any(CalendarEntity.class))).thenReturn(testResponse);

        // When
        CalendarResponse result = calendarService.create(testRequest);

        // Then
        assertNotNull(result);
        assertNotNull(testRequest.getId());
        assertFalse(testRequest.getId().isEmpty());
        verify(calendarRepository).findById("");
        verify(calendarRepository).save(any(CalendarEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when calendar with same ID already exists")
    void shouldThrowExceptionWhenCalendarAlreadyExists() {
        // Given
        when(calendarRepository.findById("test-id-123")).thenReturn(Optional.of(testEntity));

        // When & Then
        CalendarAlreadyExistsException exception = assertThrows(
            CalendarAlreadyExistsException.class,
            () -> calendarService.create(testRequest)
        );

        assertEquals("Calendar with id test-id-123 already exists", exception.getMessage());
        verify(calendarRepository).findById("test-id-123");
        verify(calendarRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should find calendar by ID")
    void shouldFindCalendarById() {
        // Given
        when(calendarRepository.findById("test-id-123")).thenReturn(Optional.of(testEntity));
        when(calendarResponseMapper.toResponse(testEntity)).thenReturn(testResponse);

        // When
        Optional<CalendarResponse> result = calendarService.findById("test-id-123");

        // Then
        assertTrue(result.isPresent());
        assertEquals("test-id-123", result.get().getId());
        verify(calendarRepository).findById("test-id-123");
        verify(calendarResponseMapper).toResponse(testEntity);
    }

    @Test
    @DisplayName("Should return empty when calendar not found")
    void shouldReturnEmptyWhenCalendarNotFound() {
        // Given
        when(calendarRepository.findById("non-existent-id")).thenReturn(Optional.empty());

        // When
        Optional<CalendarResponse> result = calendarService.findById("non-existent-id");

        // Then
        assertTrue(result.isEmpty());
        verify(calendarRepository).findById("non-existent-id");
        verify(calendarResponseMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Should delete calendar by ID")
    void shouldDeleteCalendarById() {
        // When
        calendarService.deleteById("test-id-123");

        // Then
        verify(calendarRepository).deleteById("test-id-123");
    }
}

