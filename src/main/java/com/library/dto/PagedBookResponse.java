package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "Paginated book response.")
public record PagedBookResponse(
        @Schema(description = "List of books.")
        List<BookResponseDTO> books,

        @Schema(description = "Current page (0-based index).")
        int currentPage,

        @Schema(description = "Total number of pages.")
        int totalPages,

        @Schema(description = "Total number of books.")
        long totalBooks
) {
    public static PagedBookResponse from(Page<BookResponseDTO> page) {
        return new PagedBookResponse(
                page.getContent(),
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements()
        );
    }

}
