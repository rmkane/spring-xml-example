package org.example.persistence.repository;

import java.util.List;
import java.util.Optional;

/**
 * Generic repository interface for CRUD operations.
 *
 * @param <T> the entity type
 * @param <ID> the ID type
 */
public interface Repository<T, ID> {
    /**
     * Saves an entity.
     *
     * @param entity the entity to save
     * @return the saved entity
     */
    T save(T entity);

    /**
     * Deletes an entity by its ID.
     *
     * @param id the entity ID
     */
    void deleteById(ID id);

    /**
     * Finds an entity by its ID.
     *
     * @param id the entity ID
     * @return Optional containing the entity if found, empty otherwise
     */
    Optional<T> findById(ID id);

    /**
     * Retrieves all entities.
     *
     * @return list of all entities
     */
    List<T> findAll();
}
