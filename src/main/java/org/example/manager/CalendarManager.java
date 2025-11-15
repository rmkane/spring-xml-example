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
     * @param entity the entity to save
     * @return the saved entity
     */
    Calendar save(Calendar entity);

    /**
     * Deletes a calendar entity by its ID.
     *
     * @param id the entity ID
     */
    void deleteById(String id);

    /**
     * Finds a calendar entity by its ID.
     *
     * @param id the entity ID
     * @return Optional containing the entity if found, empty otherwise
     */
    Optional<Calendar> findById(String id);

    /**
     * Retrieves all calendar entities.
     *
     * @return list of all entities
     */
    List<Calendar> findAll();
}
