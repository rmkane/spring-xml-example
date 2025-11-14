package org.example.dto.response;

import org.example.model.EntryType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "EntryResponse", description = "Metadata entry response")
public class EntryResponse {
    @Schema(description = "Entry name", example = "Entry 1")
    private String name;

    @Schema(description = "Entry count", example = "10", type = "integer")
    private Integer count;

    @Schema(description = "Entry type", example = "standard", allowableValues = {"standard", "premium", "basic"})
    private EntryType type;
}

