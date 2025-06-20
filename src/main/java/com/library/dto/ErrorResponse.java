package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * Class that models the error that is returned.
 */
@Data
@Schema(description = "Error response class for validation errors.")
@SuperBuilder
public class ErrorResponse {
    @Schema(example = "400", description = "HTTP Status Code")
    private int status;

    @Schema(example = "Input validation failed", description = "Error message")
    private String message;
}
