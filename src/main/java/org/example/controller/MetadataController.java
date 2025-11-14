package org.example.controller;

import java.util.List;

import org.example.dto.request.MetadataRequest;
import org.example.dto.response.MetadataResponse;
import org.example.exception.MetadataNotFoundException;
import org.example.service.MetadataService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MetadataController {
    private final MetadataService metadataService;

    @PostMapping(
        path = "/metadata",
        consumes = MediaType.APPLICATION_XML_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
        summary = "Create metadata",
        description = "Creates a new metadata entry from XML input",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Metadata request in XML format",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_XML_VALUE,
                schema = @Schema(implementation = MetadataRequest.class),
                examples = @ExampleObject(
                    name = "Example Metadata",
                    value = """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <metadata id="012345678-9012-3456-7890-123456789012">
                        <name>Example Metadata</name>
                        <description>This is an example</description>
                        <info>
                            <state>active</state>
                            <created-date>01/15/2025</created-date>
                            <created-time>14:30:00</created-time>
                            <created-datetime>01/15/2025 14:30:00</created-datetime>
                        </info>
                        <entries>
                            <entry>
                                <name>Entry 1</name>
                                <count>10</count>
                                <type>standard</type>
                            </entry>
                            <entry>
                                <name>Entry 2</name>
                                <count>5</count>
                                <type>premium</type>
                            </entry>
                        </entries>
                    </metadata>
                    """
                )
            )
        )
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Metadata created successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = MetadataResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - metadata with the same ID already exists",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        )
    })
    /**
     * Creates a new metadata entry from XML input.
     *
     * @param metadata the metadata request in XML format
     * @return ResponseEntity with status 201 (CREATED), Location header, and the created metadata response
     */
    public ResponseEntity<MetadataResponse> createMetadata(@RequestBody MetadataRequest metadata) {
        MetadataResponse response = metadataService.create(metadata);
        var location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.getId())
            .toUri();
        return ResponseEntity.status(HttpStatus.CREATED)
            .location(location)
            .body(response);
    }

    @GetMapping("/metadata")
    @Operation(
        summary = "Get all metadata",
        description = "Retrieves a list of all metadata entries"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved list of metadata",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = MetadataResponse.class))
            )
        )
    })
    /**
     * Retrieves all metadata entries.
     *
     * @return ResponseEntity with status 200 (OK) and a list of all metadata responses
     */
    public ResponseEntity<List<MetadataResponse>> getMetadata() {
        List<MetadataResponse> response = metadataService.findAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/metadata/{id}")
    @Operation(
        summary = "Get metadata by ID",
        description = "Retrieves a specific metadata entry by its ID"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved metadata",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = MetadataResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Metadata not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        )
    })
    /**
     * Retrieves a specific metadata entry by its ID.
     *
     * @param id the metadata ID
     * @return ResponseEntity with status 200 (OK) and the metadata response
     * @throws MetadataNotFoundException if the metadata with the given ID is not found
     */
    public ResponseEntity<MetadataResponse> getMetadata(
        @Parameter(description = "Metadata ID", required = true, example = "012345678-9012-3456-7890-123456789012")
        @PathVariable String id) {
        MetadataResponse response = metadataService.findById(id)
            .orElseThrow(() -> new MetadataNotFoundException(id));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/metadata/{id}")
    @Operation(
        summary = "Delete metadata",
        description = "Deletes a metadata entry by ID. Idempotent - returns 204 whether the resource existed or not."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Metadata deleted successfully (or did not exist)"
        )
    })
    /**
     * Deletes a metadata entry by ID. Idempotent operation - returns 204 whether the resource existed or not.
     *
     * @param id the metadata ID to delete
     * @return ResponseEntity with status 204 (NO_CONTENT)
     */
    public ResponseEntity<Void> deleteMetadata(
        @Parameter(description = "Metadata ID", required = true, example = "012345678-9012-3456-7890-123456789012")
        @PathVariable String id) {
        metadataService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

