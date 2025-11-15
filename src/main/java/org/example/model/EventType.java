package org.example.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Event type enumeration.
 * Demonstrates Jackson XML enum mapping for event types.
 */
@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum EventType {
    @JsonProperty("holiday")
    HOLIDAY,
    
    @JsonProperty("meeting")
    MEETING,
    
    @JsonProperty("appointment")
    APPOINTMENT,
    
    @JsonProperty("reminder")
    REMINDER,
    
    @JsonProperty("other")
    OTHER
}

