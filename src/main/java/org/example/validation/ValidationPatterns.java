package org.example.validation;

/**
 * Validation pattern constants for reuse across DTOs.
 */
public interface ValidationPatterns {
    /**
     * UUID pattern matching standard UUID format (8-4-4-4-12 hexadecimal digits).
     * Allows empty string for optional IDs that can be auto-generated.
     * Case-insensitive.
     */
    String UUID_PATTERN = "^$|^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";
    
    /**
     * UUID validation message.
     */
    String UUID_MESSAGE = "must be a valid UUID format (or empty to auto-generate)";
}

