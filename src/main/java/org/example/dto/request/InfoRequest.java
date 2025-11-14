package org.example.dto.request;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.example.model.MetadataState;

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
@Schema(name = "InfoRequest", description = "Metadata info request")
public class InfoRequest {
    @JacksonXmlProperty(localName = "state")
    @Schema(description = "Metadata state", example = "active", allowableValues = {"unknown", "active", "inactive"})
    private MetadataState state;

    @JacksonXmlProperty(localName = "created-date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy")
    @Schema(description = "Created date", example = "01/15/2025", type = "string", format = "date")
    private LocalDate createdDate;

    @JacksonXmlProperty(localName = "created-time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    @Schema(description = "Created time", example = "14:30:00", type = "string", format = "time")
    private LocalTime createdTime;

    @JacksonXmlProperty(localName = "created-datetime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy HH:mm:ss")
    @Schema(description = "Created datetime", example = "01/15/2025 14:30:00", type = "string", format = "date-time")
    private LocalDateTime createdDatetime;
}

