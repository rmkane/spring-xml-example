package org.example.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "MetadataResponse", description = "Metadata response")
public class MetadataResponse {
    @Schema(description = "Metadata ID", example = "012345678-9012-3456-7890-123456789012")
    private String id;

    @Schema(description = "Metadata name", example = "Example Metadata")
    private String name;

    @Schema(description = "Metadata description", example = "This is an example")
    private String description;

    @Schema(description = "Metadata info section")
    private InfoResponse info;

    @Schema(description = "List of metadata entries")
    private List<EntryResponse> entries;
}

