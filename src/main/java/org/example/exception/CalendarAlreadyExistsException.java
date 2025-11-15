package org.example.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a calendar entry with the same ID already exists.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CalendarAlreadyExistsException extends RuntimeException {
    /**
     * Constructs a new CalendarAlreadyExistsException with the given ID.
     *
     * @param id the calendar ID that already exists
     */
    public CalendarAlreadyExistsException(String id) {
        super("Calendar with id " + id + " already exists");
    }
}

