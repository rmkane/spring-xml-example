package org.example.service;

import java.util.List;
import java.util.Optional;

import org.example.dto.request.CalendarRequest;
import org.example.dto.response.CalendarResponse;

/**
 * Service interface for calendar operations.
 */
public interface CalendarService {
    /**
     * Finds a calendar entry by its ID.
     *
     * @param id the calendar ID
     * @return Optional containing the calendar response if found, empty otherwise
     */
    Optional<CalendarResponse> findById(String id);

    /**
     * Deletes a calendar entry by its ID. Idempotent operation.
     *
     * @param id the calendar ID to delete
     */
    void deleteById(String id);

    /**
     * Retrieves all calendar entries.
     *
     * @return list of all calendar responses
     */
    List<CalendarResponse> findAll();

    /**
     * Creates a new calendar entry. Generates an ID if not provided.
     *
     * @param calendar the calendar request
     * @return the created calendar response
     * @throws org.example.exception.CalendarAlreadyExistsException if a calendar entry with the same ID already exists
     */
    CalendarResponse create(CalendarRequest calendar);
}

