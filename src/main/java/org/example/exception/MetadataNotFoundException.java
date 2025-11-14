package org.example.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a metadata entry is not found.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class MetadataNotFoundException extends RuntimeException {
    /**
     * Constructs a new MetadataNotFoundException with the given ID.
     *
     * @param id the metadata ID that was not found
     */
    public MetadataNotFoundException(String id) {
        super("Metadata with id " + id + " not found");
    }
}

