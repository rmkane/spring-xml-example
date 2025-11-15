package org.example.persistence.repository;

import java.util.List;

import org.example.persistence.entity.Calendar;

/**
 * Repository interface for calendar operations.
 */
public interface CalendarRepository extends Repository<Calendar, String> {
    /**
     * Retrieves a paginated list of calendar entities.
     *
     * @param page the page number (0-indexed)
     * @param size the page size
     * @return list of calendar entities for the requested page
     */
    List<Calendar> findAll(int page, int size);
}

