package org.example.model;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Calendar state enumeration.
 * Demonstrates Jackson XML enum mapping from lowercase XML values to uppercase enum constants.
 * Uses @JsonProperty to map XML values (e.g., "active") to enum constants (e.g., ACTIVE).
 */
@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum CalendarState {
    @JsonProperty("unknown")
    @JsonEnumDefaultValue
    UNKNOWN,
    
    @JsonProperty("active")
    ACTIVE,
    
    @JsonProperty("inactive")
    INACTIVE
}

