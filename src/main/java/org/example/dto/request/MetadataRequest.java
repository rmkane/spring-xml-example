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
@JacksonXmlRootElement(localName = "metadata")
@Schema(name = "MetadataRequest", description = "Metadata request")
public class MetadataRequest {
    @JacksonXmlProperty(isAttribute = true, localName = "id")
    @Schema(description = "Metadata ID", example = "1", accessMode = AccessMode.READ_WRITE)
    private String id;

    @JacksonXmlProperty(localName = "name")
    @Schema(description = "Metadata name", example = "Example Metadata")
    private String name;

    @JacksonXmlProperty(localName = "description")
    @Schema(description = "Metadata description", example = "This is an example")
    private String description;

    @JacksonXmlProperty(localName = "info")
    @Schema(description = "Metadata info section")
    private InfoRequest info;

    @JacksonXmlProperty(localName = "entries")
    @JacksonXmlElementWrapper(localName = "entries")
    @Schema(description = "List of metadata entries")
    private List<EntryRequest> entries;
}

