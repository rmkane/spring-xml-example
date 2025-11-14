package org.example.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Entry type enumeration.
 * Demonstrates Jackson XML enum mapping for entry types.
 */
@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum EntryType {
    @JsonProperty("standard")
    STANDARD,
    
    @JsonProperty("premium")
    PREMIUM,
    
    @JsonProperty("basic")
    BASIC
}

