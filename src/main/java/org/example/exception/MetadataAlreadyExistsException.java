package org.example.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a metadata entry with the same ID already exists.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MetadataAlreadyExistsException extends RuntimeException {
    /**
     * Constructs a new MetadataAlreadyExistsException with the given ID.
     *
     * @param id the metadata ID that already exists
     */
    public MetadataAlreadyExistsException(String id) {
        super("Metadata with id " + id + " already exists");
    }
}

