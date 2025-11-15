package org.example.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "CalendarResponse", description = "Calendar response")
public class CalendarResponse {
    @Schema(description = "Calendar ID", example = "20dbf44a-b88b-4742-a0b0-1d6c7dece68d")
    private String id;

    @Schema(description = "Calendar name", example = "Work Calendar")
    private String name;

    @Schema(description = "Calendar description", example = "Work schedule and meetings")
    private String description;

    @Schema(description = "Calendar metadata section")
    private CalendarMetadataResponse metadata;

    @Schema(description = "List of calendar events")
    private List<EventResponse> events;
}

