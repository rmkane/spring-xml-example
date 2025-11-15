package org.example.dto.request;

import org.example.model.CalendarState;
import org.example.model.CalendarVisibility;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import io.swagger.v3.oas.annotations.media.Schema;
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
    private CalendarState status;

    @JacksonXmlProperty(localName = "visibility")
    @Schema(description = "Calendar visibility/sharing setting", example = "shared", allowableValues = {"personal", "shared", "private"})
    private CalendarVisibility visibility;

    @JacksonXmlProperty(localName = "created-at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy HH:mm:ss")
    @Schema(description = "Created at timestamp", example = "11/13/2025 12:00:00", type = "string", format = "date-time")
    private String createdAt;

    @JacksonXmlProperty(localName = "created-by")
    @Schema(description = "Created by user", example = "John Doe")
    private String createdBy;

    @JacksonXmlProperty(localName = "updated-at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy HH:mm:ss")
    @Schema(description = "Updated at timestamp", example = "11/13/2025 14:30:00", type = "string", format = "date-time")
    private String updatedAt;

    @JacksonXmlProperty(localName = "updated-by")
    @Schema(description = "Updated by user", example = "Jane Doe")
    private String updatedBy;

    @JacksonXmlProperty(localName = "count")
    @Schema(description = "Event count", example = "2", type = "integer")
    private Integer count;
}

