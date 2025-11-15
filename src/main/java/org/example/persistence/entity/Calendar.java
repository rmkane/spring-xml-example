package org.example.persistence.entity;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Calendar {
    private String id;
    private String name;
    private String description;
    private CalendarMetadata metadata;
    private List<CalendarEvent> events;
}

