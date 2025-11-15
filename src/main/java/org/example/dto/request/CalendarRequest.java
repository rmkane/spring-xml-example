package org.example.dto.request;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "calendar")
@Schema(name = "CalendarRequest", description = "Calendar request")
public class CalendarRequest {
    @JacksonXmlProperty(isAttribute = true, localName = "id")
    @Schema(description = "Calendar ID", example = "20dbf44a-b88b-4742-a0b0-1d6c7dece68d", accessMode = AccessMode.READ_WRITE)
    private String id;

    @JacksonXmlProperty(localName = "name")
    @Schema(description = "Calendar name", example = "Work Calendar")
    private String name;

    @JacksonXmlProperty(localName = "description")
    @Schema(description = "Calendar description", example = "Work schedule and meetings")
    private String description;

    @JacksonXmlProperty(localName = "metadata")
    @Schema(description = "Calendar metadata section")
    private CalendarMetadataRequest metadata;

    @JacksonXmlProperty(localName = "event")
    @JacksonXmlElementWrapper(useWrapping = false)
    @Schema(description = "List of calendar events")
    private List<EventRequest> events;
}

