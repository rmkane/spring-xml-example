package org.example.model;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Calendar visibility/sharing enumeration.
 * Demonstrates Jackson XML enum mapping for calendar visibility settings.
 */
@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum CalendarVisibility {
    @JsonProperty("personal")
    @JsonEnumDefaultValue
    PERSONAL,
    
    @JsonProperty("shared")
    SHARED,
    
    @JsonProperty("private")
    PRIVATE
}

