package com.library.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception when Object ID is invalid, returns 400 Response.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidObjectIdException extends RuntimeException{

    /**
     * Constructor.
     * @param id the invalid object ID.
     */
    public InvalidObjectIdException(final String id) {
        super(String.format("The ID: %s is not valid, " +
                "it must be must be a valid 24-character hex string", id));
    }
}
