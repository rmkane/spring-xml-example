package org.example.dto.request;

import org.example.model.EntryType;

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
@JacksonXmlRootElement(localName = "entry")
@Schema(name = "EntryRequest", description = "Metadata entry request")
public class EntryRequest {
    @JacksonXmlProperty(localName = "name")
    @Schema(description = "Entry name", example = "Entry 1")
    private String name;

    @JacksonXmlProperty(localName = "count")
    @Schema(description = "Entry count", example = "10", type = "integer")
    private Integer count;

    @JacksonXmlProperty(localName = "type")
    @Schema(description = "Entry type", example = "standard", allowableValues = {"standard", "premium", "basic"})
    private EntryType type;
}

