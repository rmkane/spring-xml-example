package org.example.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.example.dto.request.CalendarRequest;
import org.example.dto.request.InfoRequest;
import org.example.dto.response.CalendarResponse;
import org.example.exception.CalendarAlreadyExistsException;
import org.example.mapper.CalendarRequestMapper;
import org.example.mapper.CalendarResponseMapper;
import org.example.model.CalendarState;
import org.example.persistence.entity.CalendarEntity;
import org.example.persistence.repository.CalendarRepository;
import org.example.service.CalendarService;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService {
    private final CalendarRepository calendarRepository;
    private final CalendarRequestMapper calendarRequestMapper;
    private final CalendarResponseMapper calendarResponseMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<CalendarResponse> findById(String id) {
        return calendarRepository.findById(id)
            .map(calendarResponseMapper::toResponse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteById(String id) {
        calendarRepository.deleteById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CalendarResponse> findAll() {
        return calendarRepository.findAll()
            .stream()
            .map(calendarResponseMapper::toResponse)
            .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CalendarResponse create(CalendarRequest calendar) {
        if (calendarRepository.findById(calendar.getId()).isPresent()) {
            throw new CalendarAlreadyExistsException(calendar.getId());
        }
        if (calendar.getId() == null || calendar.getId().isEmpty()) {
            calendar.setId(generateId());
        }
        // Set default status from metadata if not provided
        if (calendar.getMetadata() != null && calendar.getMetadata().getStatus() == null) {
            calendar.getMetadata().setStatus(CalendarState.UNKNOWN);
        } else if (calendar.getMetadata() == null) {
            InfoRequest metadataInfo = InfoRequest.builder()
                .status(CalendarState.UNKNOWN)
                .build();
            calendar.setMetadata(metadataInfo);
        }
        CalendarEntity entity = calendarRequestMapper.toEntity(calendar);
        return calendarResponseMapper.toResponse(calendarRepository.save(entity));
    }

    /**
     * Generates a unique UUID for calendar entries.
     *
     * @return a UUID string
     */
    private String generateId() {
        return UUID.randomUUID().toString();
    }
}

