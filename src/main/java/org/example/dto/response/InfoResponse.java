package org.example.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.example.model.MetadataState;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "InfoResponse", description = "Metadata info response")
public class InfoResponse {
    @Schema(description = "Metadata state", example = "active", allowableValues = {"unknown", "active", "inactive"})
    private MetadataState state;

    @Schema(description = "Created date", example = "01/15/2025", type = "string", format = "date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy")
    private LocalDate createdDate;

    @Schema(description = "Created time", example = "14:30:00", type = "string", format = "time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime createdTime;

    @Schema(description = "Created datetime", example = "01/15/2025 14:30:00", type = "string", format = "date-time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy HH:mm:ss")
    private LocalDateTime createdDatetime;
}

