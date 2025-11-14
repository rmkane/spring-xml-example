package org.example.service;

import java.util.List;
import java.util.Optional;

import org.example.dto.request.MetadataRequest;
import org.example.dto.response.MetadataResponse;

/**
 * Service interface for metadata operations.
 */
public interface MetadataService {
    /**
     * Finds a metadata entry by its ID.
     *
     * @param id the metadata ID
     * @return Optional containing the metadata response if found, empty otherwise
     */
    Optional<MetadataResponse> findById(String id);

    /**
     * Deletes a metadata entry by its ID. Idempotent operation.
     *
     * @param id the metadata ID to delete
     */
    void deleteById(String id);

    /**
     * Retrieves all metadata entries.
     *
     * @return list of all metadata responses
     */
    List<MetadataResponse> findAll();

    /**
     * Creates a new metadata entry. Generates an ID if not provided.
     *
     * @param metadata the metadata request
     * @return the created metadata response
     * @throws org.example.exception.MetadataAlreadyExistsException if a metadata entry with the same ID already exists
     */
    MetadataResponse create(MetadataRequest metadata);
}

