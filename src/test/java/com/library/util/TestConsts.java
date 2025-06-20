package com.library.util;

import java.time.format.DateTimeFormatter;

/**
 * Util class containing constants used in multiple tests.
 */
public class TestConsts {

    /**
     * The books API path.
     */
    public static final String API_BOOKS_PATH = "/books";

    /**
     * Formatter to parse string into date.
     */
    public static final DateTimeFormatter DATE_FORMATTER
            = DateTimeFormatter.ofPattern("dd-MM-yyyy");
}
