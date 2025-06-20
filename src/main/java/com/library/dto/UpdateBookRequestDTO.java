package com.library.dto;

import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDate;

/**
 * Defines the update book request object.
 */
@Data
@Builder
public class UpdateBookRequestDTO {

    /**
     * Title of book.
     */
    private String title;

    /**
     * Book author.
     */
    private String author;

    /**
     * The ISBN of the book.
     */
    @Indexed(unique = true)
    @Pattern(regexp = "\\d{13}", message = "ISBN must be exactly 13 digits")
    private String isbn;

    /**
     * The date the book was published.
     */
    @PastOrPresent
    private LocalDate publishedDate;
}
