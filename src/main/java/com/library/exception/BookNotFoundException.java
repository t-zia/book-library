package com.library.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception when Book ID does not exist returns 404 Response.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class BookNotFoundException extends RuntimeException {

    /**
     * Constructor
     * @param id the ID of the book.
     */
    public BookNotFoundException(final String id) {
        super(String.format("Book with ID: %s was not found", id));
    }
}
