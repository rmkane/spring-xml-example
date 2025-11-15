package org.example.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a calendar entry is not found.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class CalendarNotFoundException extends RuntimeException {
    /**
     * Constructs a new CalendarNotFoundException with the given ID.
     *
     * @param id the calendar ID that was not found
     */
    public CalendarNotFoundException(String id) {
        super("Calendar with id " + id + " not found");
    }
}

