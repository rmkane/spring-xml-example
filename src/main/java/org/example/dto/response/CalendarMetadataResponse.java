package org.example.dto.response;

import org.example.model.CalendarState;
import org.example.model.CalendarVisibility;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "CalendarMetadataResponse", description = "Calendar metadata response")
public class CalendarMetadataResponse {
    @Schema(description = "Calendar status", example = "active", allowableValues = {"unknown", "active", "inactive"})
    private CalendarState status;

    @Schema(description = "Calendar visibility/sharing setting", example = "shared", allowableValues = {"personal", "shared", "private"})
    private CalendarVisibility visibility;

    @Schema(description = "Created at timestamp", example = "11/13/2025 12:00:00", type = "string", format = "date-time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy HH:mm:ss")
    private String createdAt;

    @Schema(description = "Created by user", example = "John Doe")
    private String createdBy;

    @Schema(description = "Updated at timestamp", example = "11/13/2025 14:30:00", type = "string", format = "date-time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy HH:mm:ss")
    private String updatedAt;

    @Schema(description = "Updated by user", example = "Jane Doe")
    private String updatedBy;

    @Schema(description = "Event count", example = "2", type = "integer")
    private Integer count;
}

