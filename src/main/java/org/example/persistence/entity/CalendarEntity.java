package org.example.persistence.entity;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEntity {
    private String id;
    private String name;
    private String description;
    private InfoEntity metadata;
    private List<EventEntity> events;
}

