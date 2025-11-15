package org.example.exception;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.dao.DuplicateKeyException;
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

    /**
     * Handles duplicate key exceptions from the database.
     * This occurs when trying to insert a record with a key that already exists.
     *
     * @param e the duplicate key exception
     * @return ProblemDetail with status 409 (CONFLICT) and error message
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public ProblemDetail handleDuplicateKeyException(DuplicateKeyException e) {
        String message = extractDuplicateKeyMessage(e);
        log.warn("Duplicate key violation: {}", message);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message);
        problemDetail.setTitle("Duplicate Key Violation");
        return problemDetail;
    }

    /**
     * Extracts a user-friendly message from a DuplicateKeyException.
     * Attempts to parse the PostgreSQL error message to identify the table and key.
     *
     * @param e the duplicate key exception
     * @return a user-friendly error message
     */
    private String extractDuplicateKeyMessage(DuplicateKeyException e) {
        String originalMessage = e.getMessage();
        if (originalMessage == null) {
            return "A record with the same key already exists";
        }

        // Try to extract table and key information from PostgreSQL error messages
        // Pattern: "ERROR: duplicate key value violates unique constraint \"constraint_name\"\n  Detail: Key (column)=(value) already exists."
        Pattern constraintPattern = Pattern.compile("constraint \"([^\"]+)\"");
        Pattern keyPattern = Pattern.compile("Key \\(([^)]+)\\)=\\(([^)]+)\\)");
        
        Matcher constraintMatcher = constraintPattern.matcher(originalMessage);
        Matcher keyMatcher = keyPattern.matcher(originalMessage);
        
        String constraintName = null;
        String column = null;
        String value = null;
        
        if (constraintMatcher.find()) {
            constraintName = constraintMatcher.group(1);
        }
        
        if (keyMatcher.find()) {
            column = keyMatcher.group(1);
            value = keyMatcher.group(2);
        }
        
        // Determine table name from constraint name (e.g., "events_pkey" -> "events")
        String table = null;
        if (constraintName != null) {
            if (constraintName.contains("events")) {
                table = "event";
            } else if (constraintName.contains("calendars")) {
                table = "calendar";
            }
        }
        
        // Build user-friendly message
        if (table != null && column != null && value != null) {
            return String.format("A %s with %s '%s' already exists", table, column, value);
        } else if (table != null) {
            return String.format("A %s with the same identifier already exists", table);
        } else {
            return "A record with the same key already exists: " + originalMessage;
        }
    }
}

