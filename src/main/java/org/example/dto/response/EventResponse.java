package org.example.dto.response;

import java.time.LocalDateTime;

import org.example.model.EventType;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "EventResponse", description = "Calendar event response")
public class EventResponse {
    @Schema(description = "Event ID", example = "a1b2c3d4-e5f6-7890-abcd-111111111111")
    private String id;

    @Schema(description = "Event name", example = "Team Standup")
    private String name;

    @Schema(description = "Event description", example = "Daily team standup meeting")
    private String description;

    @Schema(description = "Event type", example = "meeting", allowableValues = {"holiday", "meeting", "appointment", "reminder", "other"})
    private EventType type;

    @Schema(description = "Whether the event is disabled", example = "false", type = "boolean")
    private Boolean disabled;

    @Schema(description = "Whether the event is an all-day event", example = "false", type = "boolean")
    private Boolean allDay;

    @Schema(description = "Event start date and time", example = "11/14/2025 09:00:00", type = "string", format = "date-time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy HH:mm:ss")
    private LocalDateTime startDateTime;

    @Schema(description = "Event end date and time", example = "11/14/2025 09:30:00", type = "string", format = "date-time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy HH:mm:ss")
    private LocalDateTime endDateTime;

    @Schema(description = "Event location", example = "Zoom")
    private String location;

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
}
