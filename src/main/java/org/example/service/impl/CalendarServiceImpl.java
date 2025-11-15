package org.example.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.example.dto.request.CalendarRequest;
import org.example.dto.request.CalendarMetadataRequest;
import org.example.dto.response.CalendarResponse;
import org.example.dto.response.PagedResponse;
import org.example.exception.CalendarAlreadyExistsException;
import org.example.manager.CalendarManager;
import org.example.persistence.entity.Calendar;
import org.example.mapper.CalendarRequestMapper;
import org.example.mapper.CalendarResponseMapper;
import org.example.model.CalendarState;
import org.example.service.CalendarService;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService {
    private final CalendarManager calendarManager;
    private final CalendarRequestMapper calendarRequestMapper;
    private final CalendarResponseMapper calendarResponseMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<CalendarResponse> findById(String id) {
        log.info("Finding calendar by ID: {}", id);
        Optional<CalendarResponse> result = calendarManager.findCalendarById(id)
            .map(calendarResponseMapper::toResponse);
        if (result.isPresent()) {
            log.debug("Calendar found: id={}, name={}, eventCount={}", 
                id, 
                result.get().getName(),
                result.get().getEvents() != null ? result.get().getEvents().size() : 0);
        } else {
            log.debug("Calendar not found: id={}", id);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteById(String id) {
        log.info("Deleting calendar: id={}", id);
        boolean exists = calendarManager.calendarExists(id);
        if (exists) {
            calendarManager.deleteCalendarById(id);
            log.info("Calendar deleted successfully: id={}", id);
        } else {
            log.warn("Attempted to delete non-existent calendar: id={}", id);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CalendarResponse> findAll() {
        log.info("Finding all calendars");
        List<CalendarResponse> results = calendarManager.findAllCalendars()
            .stream()
            .map(calendarResponseMapper::toResponse)
            .toList();
        log.info("Found {} calendar(s)", results.size());
        log.debug("Calendar IDs: {}", results.stream().map(CalendarResponse::getId).toList());
        return results;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PagedResponse<CalendarResponse> findAll(int page, int size) {
        log.info("Finding calendars: page={}, size={}", page, size);
        
        // Get total count
        long totalElements = calendarManager.getCalendarCount();
        
        // Calculate total pages
        int totalPages = (int) Math.ceil((double) totalElements / size);
        
        // Get paginated results
        List<CalendarResponse> items = calendarManager.findAllCalendars(page, size)
            .stream()
            .map(calendarResponseMapper::toResponse)
            .toList();
        
        boolean first = page == 0;
        boolean last = page >= totalPages - 1 || items.isEmpty();
        
        PagedResponse<CalendarResponse> response = PagedResponse.<CalendarResponse>builder()
            .items(items)
            .page(page)
            .size(size)
            .totalElements(totalElements)
            .totalPages(totalPages)
            .first(first)
            .last(last)
            .build();
        
        log.info("Found {} calendar(s) on page {} of {} (total: {})", 
            items.size(), page, totalPages, totalElements);
        log.debug("Calendar IDs: {}", items.stream().map(CalendarResponse::getId).toList());
        
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CalendarResponse create(CalendarRequest calendar) {
        String calendarId = calendar.getId();
        log.info("Creating calendar: id={}, name={}", calendarId, calendar.getName());
        
        // Check if calendar already exists
        if (calendarId != null && !calendarId.isEmpty() && calendarManager.findCalendarById(calendarId).isPresent()) {
            log.warn("Calendar already exists: id={}", calendarId);
            throw new CalendarAlreadyExistsException(calendarId);
        }
        
        // Generate ID if not provided
        if (calendarId == null || calendarId.isEmpty()) {
            calendarId = generateId();
            calendar.setId(calendarId);
            log.debug("Generated new calendar ID: {}", calendarId);
        }
        
        // Set default status from metadata if not provided
        if (calendar.getMetadata() != null && calendar.getMetadata().getStatus() == null) {
            calendar.getMetadata().setStatus(CalendarState.UNKNOWN);
            log.debug("Set default status to UNKNOWN for calendar: id={}", calendarId);
        } else if (calendar.getMetadata() == null) {
            CalendarMetadataRequest metadataRequest = CalendarMetadataRequest.builder()
                .status(CalendarState.UNKNOWN)
                .build();
            calendar.setMetadata(metadataRequest);
            log.debug("Created default metadata for calendar: id={}", calendarId);
        }
        
        int eventCount = calendar.getEvents() != null ? calendar.getEvents().size() : 0;
        log.debug("Creating calendar with {} event(s): id={}, status={}, visibility={}", 
            eventCount,
            calendarId,
            calendar.getMetadata().getStatus(),
            calendar.getMetadata().getVisibility());
        
        Calendar entity = calendarRequestMapper.toEntity(calendar);
        CalendarResponse response = calendarResponseMapper.toResponse(calendarManager.saveCalendar(entity));
        
        log.info("Calendar created successfully: id={}, name={}, eventCount={}", 
            response.getId(), 
            response.getName(),
            response.getEvents() != null ? response.getEvents().size() : 0);
        
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAll() {
        log.info("Deleting all calendars");
        calendarManager.deleteAllCalendars();
        log.info("All calendars deleted successfully");
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

