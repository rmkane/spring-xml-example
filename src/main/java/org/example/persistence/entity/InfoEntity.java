package org.example.persistence.entity;

import org.example.model.CalendarState;
import org.example.model.CalendarVisibility;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InfoEntity {
    private CalendarState status;
    private CalendarVisibility visibility;
    private String createdAt;
    private String createdBy;
    private String updatedAt;
    private String updatedBy;
    private Integer count;
}
