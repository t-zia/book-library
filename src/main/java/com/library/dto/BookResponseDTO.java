package com.library.dto;

import com.library.model.Book;
import lombok.Builder;
import lombok.Data;

/**
 * Response class that defines the information shared in a {@link Book} response.
 */
@Data
@Builder
public class BookResponseDTO {

    /**
     * The book ID.
     */
    private String id;

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
    private String isbn;

    /**
     * The date the book was published.
     */
    private String publishedDate;
}
