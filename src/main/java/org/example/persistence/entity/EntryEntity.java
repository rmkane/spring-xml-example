package org.example.persistence.entity;

import org.example.model.EntryType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntryEntity {
    private String name;
    private Integer count;
    private EntryType type;
}

