package org.example.persistence.entity;

import java.time.LocalDateTime;

import org.example.model.EventType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventEntity {
    private String id;
    private String name;
    private String description;
    private EventType type;
    private Boolean disabled;
    private Boolean allDay;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String location;
    private String createdAt;
    private String createdBy;
    private String updatedAt;
    private String updatedBy;
}
