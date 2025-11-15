package org.example.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Paginated response wrapper for list endpoints.
 *
 * @param <T> the type of items in the page
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Paginated response containing items and pagination metadata")
public class PagedResponse<T> {
    @Schema(description = "List of items in the current page", required = true)
    private List<T> items;

    @Schema(description = "Current page number (0-indexed)", example = "0", required = true)
    private int page;

    @Schema(description = "Number of items per page", example = "10", required = true)
    private int size;

    @Schema(description = "Total number of items across all pages", example = "42", required = true)
    private long totalElements;

    @Schema(description = "Total number of pages", example = "5", required = true)
    private int totalPages;

    @Schema(description = "Whether this is the first page", example = "true", required = true)
    private boolean first;

    @Schema(description = "Whether this is the last page", example = "false", required = true)
    private boolean last;
}

