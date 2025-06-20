package com.library.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Class to model the book entity.
 */
@Document(collection = "books")
@RequiredArgsConstructor
@Data
public class Book implements Serializable {

    /**
     * The book ID.
     */
    @Id
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String id;

    /**
     * Title of the book.
     */
    @NotBlank
    @NonNull
    private String title;

    /**
     * Book author.
     */
    @NotBlank
    @NonNull
    private String author;

    /**
     * The ISBN of the book.
     */
    @NotBlank
    @Indexed(unique = true)
    @Size(min = 13, max = 13)
    @NonNull
    private String isbn;

    /**
     * The date the book was published.
     */
    @PastOrPresent
    @NonNull
    private LocalDate publishedDate;
}
