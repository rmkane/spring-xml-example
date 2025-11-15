package org.example.manager;

import java.util.List;
import java.util.Optional;

import org.example.persistence.entity.Calendar;

/**
 * Manager interface for calendar operations.
 */
public interface CalendarManager {
    /**
     * Saves a calendar entity.
     *
     * @param calendar the calendar entity to save
     * @return the saved calendar entity
     */
    Calendar saveCalendar(Calendar calendar);

    /**
     * Deletes a calendar entity by its ID.
     *
     * @param id the calendar ID
     */
    void deleteCalendarById(String id);

    /**
     * Finds a calendar entity by its ID.
     *
     * @param id the calendar ID
     * @return Optional containing the calendar entity if found, empty otherwise
     */
    Optional<Calendar> findCalendarById(String id);

    /**
     * Retrieves all calendar entities.
     *
     * @return list of all calendar entities
     */
    List<Calendar> findAllCalendars();

    /**
     * Checks if a calendar entity exists by its ID.
     *
     * @param id the calendar ID
     * @return true if the calendar exists, false otherwise
     */
    boolean calendarExists(String id);

    /**
     * Returns the number of calendar entities.
     *
     * @return the number of calendars
     */
    long getCalendarCount();
}
