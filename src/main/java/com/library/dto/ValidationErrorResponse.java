package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Defines a response class used when input validation fails.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class ValidationErrorResponse extends ErrorResponse {

    /**
     * List of validation errors.
     */
    @Schema(description = "Detailed parameter errors")
    private List<ValidationError> errors;

    /**
     * Represents a single field validation error.
     */
    @Data
    @AllArgsConstructor
    public static class ValidationError {
        @Schema(example = "author", description = "Parameter name.")
        private String parameter;

        @Schema(example = "must not be null", description = "Detailed error message.")
        private String message;

        @Override
        public String toString() {
            return String.format("Parameter: %s\n Message: %s", parameter, message);
        }
    }
}
