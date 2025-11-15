package org.example.dto.request;

import org.example.model.CalendarState;
import org.example.model.CalendarVisibility;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CalendarMetadataRequest", description = "Calendar metadata request")
public class CalendarMetadataRequest {
    @JacksonXmlProperty(localName = "status")
    @Schema(description = "Calendar status", example = "active", allowableValues = {"unknown", "active", "inactive"})
    @NotNull(message = "Calendar status is required")
    private CalendarState status;

    @JacksonXmlProperty(localName = "visibility")
    @Schema(description = "Calendar visibility/sharing setting", example = "shared", allowableValues = {"personal", "shared", "private"})
    @NotNull(message = "Calendar visibility is required")
    private CalendarVisibility visibility;

    @JacksonXmlProperty(localName = "created-at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy HH:mm:ss")
    @Schema(description = "Created at timestamp", example = "11/13/2025 12:00:00", type = "string", format = "date-time")
    private String createdAt;

    @JacksonXmlProperty(localName = "created-by")
    @Schema(description = "Created by user", example = "John Doe")
    @Size(max = 255, message = "Created by must not exceed 255 characters")
    private String createdBy;

    @JacksonXmlProperty(localName = "updated-at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy HH:mm:ss")
    @Schema(description = "Updated at timestamp", example = "11/13/2025 14:30:00", type = "string", format = "date-time")
    private String updatedAt;

    @JacksonXmlProperty(localName = "updated-by")
    @Schema(description = "Updated by user", example = "Jane Doe")
    @Size(max = 255, message = "Updated by must not exceed 255 characters")
    private String updatedBy;

    @JacksonXmlProperty(localName = "count")
    @Schema(description = "Event count", example = "2", type = "integer")
    @Min(value = 0, message = "Event count must be non-negative")
    private Integer count;
}

