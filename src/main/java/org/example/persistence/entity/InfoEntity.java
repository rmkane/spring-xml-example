package org.example.persistence.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.example.model.MetadataState;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InfoEntity {
    private MetadataState state;
    private LocalDate createdDate;
    private LocalTime createdTime;
    private LocalDateTime createdDatetime;
}

