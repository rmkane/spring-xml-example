package org.example;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("MetadataService Unit Tests")
class MetadataServiceImplTest {

    @Mock
    private MetadataRepository metadataRepository;

    @Mock
    private MetadataRequestMapper metadataRequestMapper;

    @Mock
    private MetadataResponseMapper metadataResponseMapper;

    @InjectMocks
    private MetadataServiceImpl metadataService;

    private MetadataRequest testRequest;
    private MetadataEntity testEntity;
    private MetadataResponse testResponse;

    @BeforeEach
    void setUp() {
        testRequest = new MetadataRequest();
        testRequest.setId("test-id-123");
        testRequest.setName("Test Metadata");
        testRequest.setDescription("Test Description");
        testRequest.setState(MetadataState.ACTIVE);
        testRequest.setCreatedDate(LocalDate.of(2025, 1, 15));
        testRequest.setCreatedTime(LocalTime.of(14, 30, 0));
        testRequest.setCreatedDatetime(LocalDateTime.of(2025, 1, 15, 14, 30, 0));

        testEntity = new MetadataEntity(
            "test-id-123",
            "Test Metadata",
            "Test Description",
            MetadataState.ACTIVE,
            LocalDate.of(2025, 1, 15),
            LocalTime.of(14, 30, 0),
            LocalDateTime.of(2025, 1, 15, 14, 30, 0)
        );

        testResponse = new MetadataResponse();
        testResponse.setId("test-id-123");
        testResponse.setName("Test Metadata");
        testResponse.setDescription("Test Description");
        testResponse.setState(MetadataState.ACTIVE);
        testResponse.setCreatedDate(LocalDate.of(2025, 1, 15));
        testResponse.setCreatedTime(LocalTime.of(14, 30, 0));
        testResponse.setCreatedDatetime(LocalDateTime.of(2025, 1, 15, 14, 30, 0));
    }

    @Test
    @DisplayName("Should create metadata successfully when ID is provided")
    void shouldCreateMetadataWithProvidedId() {
        // Given
        when(metadataRepository.findById("test-id-123")).thenReturn(Optional.empty());
        when(metadataRequestMapper.toEntity(testRequest)).thenReturn(testEntity);
        when(metadataRepository.save(testEntity)).thenReturn(testEntity);
        when(metadataResponseMapper.toResponse(testEntity)).thenReturn(testResponse);

        // When
        MetadataResponse result = metadataService.create(testRequest);

        // Then
        assertNotNull(result);
        assertEquals("test-id-123", result.getId());
        assertEquals("Test Metadata", result.getName());
        verify(metadataRepository).findById("test-id-123");
        verify(metadataRequestMapper).toEntity(testRequest);
        verify(metadataRepository).save(testEntity);
        verify(metadataResponseMapper).toResponse(testEntity);
    }

    @Test
    @DisplayName("Should generate ID when ID is null")
    void shouldGenerateIdWhenIdIsNull() {
        // Given
        testRequest.setId(null);
        when(metadataRepository.findById(null)).thenReturn(Optional.empty());
        when(metadataRequestMapper.toEntity(any(MetadataRequest.class))).thenReturn(testEntity);
        when(metadataRepository.save(any(MetadataEntity.class))).thenReturn(testEntity);
        when(metadataResponseMapper.toResponse(any(MetadataEntity.class))).thenReturn(testResponse);

        // When
        MetadataResponse result = metadataService.create(testRequest);

        // Then
        assertNotNull(result);
        assertNotNull(testRequest.getId()); // ID should be generated
        assertFalse(testRequest.getId().isEmpty());
        verify(metadataRepository).findById(null);
        verify(metadataRepository).save(any(MetadataEntity.class));
    }

    @Test
    @DisplayName("Should generate ID when ID is empty")
    void shouldGenerateIdWhenIdIsEmpty() {
        // Given
        testRequest.setId("");
        when(metadataRepository.findById("")).thenReturn(Optional.empty());
        when(metadataRequestMapper.toEntity(any(MetadataRequest.class))).thenReturn(testEntity);
        when(metadataRepository.save(any(MetadataEntity.class))).thenReturn(testEntity);
        when(metadataResponseMapper.toResponse(any(MetadataEntity.class))).thenReturn(testResponse);

        // When
        MetadataResponse result = metadataService.create(testRequest);

        // Then
        assertNotNull(result);
        assertNotNull(testRequest.getId());
        assertFalse(testRequest.getId().isEmpty());
        verify(metadataRepository).findById("");
        verify(metadataRepository).save(any(MetadataEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when metadata with same ID already exists")
    void shouldThrowExceptionWhenMetadataAlreadyExists() {
        // Given
        when(metadataRepository.findById("test-id-123")).thenReturn(Optional.of(testEntity));

        // When & Then
        MetadataAlreadyExistsException exception = assertThrows(
            MetadataAlreadyExistsException.class,
            () -> metadataService.create(testRequest)
        );

        assertEquals("Metadata with id test-id-123 already exists", exception.getMessage());
        verify(metadataRepository).findById("test-id-123");
        verify(metadataRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should find metadata by ID")
    void shouldFindMetadataById() {
        // Given
        when(metadataRepository.findById("test-id-123")).thenReturn(Optional.of(testEntity));
        when(metadataResponseMapper.toResponse(testEntity)).thenReturn(testResponse);

        // When
        Optional<MetadataResponse> result = metadataService.findById("test-id-123");

        // Then
        assertTrue(result.isPresent());
        assertEquals("test-id-123", result.get().getId());
        verify(metadataRepository).findById("test-id-123");
        verify(metadataResponseMapper).toResponse(testEntity);
    }

    @Test
    @DisplayName("Should return empty when metadata not found")
    void shouldReturnEmptyWhenMetadataNotFound() {
        // Given
        when(metadataRepository.findById("non-existent-id")).thenReturn(Optional.empty());

        // When
        Optional<MetadataResponse> result = metadataService.findById("non-existent-id");

        // Then
        assertTrue(result.isEmpty());
        verify(metadataRepository).findById("non-existent-id");
        verify(metadataResponseMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Should delete metadata by ID")
    void shouldDeleteMetadataById() {
        // When
        metadataService.deleteById("test-id-123");

        // Then
        verify(metadataRepository).deleteById("test-id-123");
    }
}

