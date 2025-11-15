package org.example.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * Global exception handler for the application.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Handles CalendarNotFoundException and returns a ProblemDetail response.
     *
     * @param e the exception that was thrown
     * @return ProblemDetail with status 404 (NOT_FOUND) and the exception message
     */
    @ExceptionHandler(CalendarNotFoundException.class)
    public ProblemDetail handleCalendarNotFoundException(CalendarNotFoundException e) {
        log.warn("Calendar not found: {}", e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    /**
     * Handles CalendarAlreadyExistsException and returns a ProblemDetail response.
     *
     * @param e the exception that was thrown
     * @return ProblemDetail with status 400 (BAD_REQUEST) and the exception message
     */
    @ExceptionHandler(CalendarAlreadyExistsException.class)
    public ProblemDetail handleCalendarAlreadyExistsException(CalendarAlreadyExistsException e) {
        log.warn("Calendar already exists: {}", e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}

