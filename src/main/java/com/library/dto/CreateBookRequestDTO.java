package com.library.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDate;

/**
 * Defines the create book request object.
 */
@Data
@Builder
public class CreateBookRequestDTO {

    /**
     * Title of the book.
     */
    @NotBlank(message = "Title is required.")
    private String title;

    /**
     * Book author.
     */
    @NotBlank(message = "Author is required.")
    private String author;

    /**
     * The ISBN of the book.
     */
    @NotBlank(message = "ISBN is required.")
    @Indexed(unique = true)
    @Pattern(regexp = "\\d{13}", message = "ISBN must be exactly 13 digits")
    private String isbn;

    /**
     * The date the book was published.
     */
    @PastOrPresent(message = "Published date cannot be in future.")
    @NotNull(message = "Published date is required.")
    private LocalDate publishedDate;
}
