package org.example.dto.request;

import java.time.LocalDateTime;

import org.example.model.EventType;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "event")
@Schema(name = "EventRequest", description = "Calendar event request")
public class EventRequest {
    @JacksonXmlProperty(isAttribute = true, localName = "id")
    @Schema(description = "Event ID", example = "a1b2c3d4-e5f6-7890-abcd-111111111111", accessMode = Schema.AccessMode.READ_WRITE)
    private String id;

    @JacksonXmlProperty(localName = "name")
    @Schema(description = "Event name", example = "Team Standup")
    private String name;

    @JacksonXmlProperty(localName = "description")
    @Schema(description = "Event description", example = "Daily team standup meeting")
    private String description;

    @JacksonXmlProperty(localName = "type")
    @Schema(description = "Event type", example = "meeting", allowableValues = {"holiday", "meeting", "appointment", "reminder", "other"})
    private EventType type;

    @JacksonXmlProperty(localName = "disabled")
    @Schema(description = "Whether the event is disabled", example = "false", type = "boolean")
    private Boolean disabled;

    @JacksonXmlProperty(localName = "all-day")
    @Schema(description = "Whether the event is an all-day event", example = "false", type = "boolean")
    private Boolean allDay;

    @JacksonXmlProperty(localName = "start-datetime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy HH:mm:ss")
    @Schema(description = "Event start date and time", example = "11/14/2025 09:00:00", type = "string", format = "date-time")
    private LocalDateTime startDateTime;

    @JacksonXmlProperty(localName = "end-datetime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy HH:mm:ss")
    @Schema(description = "Event end date and time", example = "11/14/2025 09:30:00", type = "string", format = "date-time")
    private LocalDateTime endDateTime;

    @JacksonXmlProperty(localName = "location")
    @Schema(description = "Event location", example = "Zoom")
    private String location;

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
}
