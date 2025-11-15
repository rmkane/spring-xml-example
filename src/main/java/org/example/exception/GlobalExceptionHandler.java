package org.example.exception;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    /**
     * Handles validation errors from @Valid annotations.
     *
     * @param e the validation exception
     * @return ProblemDetail with status 400 (BAD_REQUEST) and validation error details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException e) {
        String errors = e.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
            .collect(Collectors.joining(", "));
        
        String message = "Validation failed: " + errors;
        log.warn("Validation failed: {}", errors);
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        problemDetail.setProperty("errors", e.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value"
            )));
        return problemDetail;
    }

    /**
     * Handles XML parsing errors (malformed XML, invalid format, etc.).
     *
     * @param e the XML parsing exception
     * @return ProblemDetail with status 400 (BAD_REQUEST) and error message
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleXmlParsingErrors(HttpMessageNotReadableException e) {
        String message = "Invalid XML format: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        log.warn("XML parsing error: {}", message);
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
    }
}

