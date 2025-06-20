package com.library.advice;

import com.library.dto.ErrorResponse;
import com.library.dto.ValidationErrorResponse;
import com.library.dto.ValidationErrorResponse.ValidationError;
import com.library.exception.BookNotFoundException;
import com.library.exception.InvalidObjectIdException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.MongoWriteException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * This class handles exceptions thrown from controller methods.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles exceptions when Book is not found.
     *
     * @param ex the exception thrown.
     * @return 404 response with message details.
     */
    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBookNotFoundException(BookNotFoundException ex) {
        final String errorMessage = ex.getMessage();
        final ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(errorMessage)
                .build();
        log.warn("Book does not exist {}", errorMessage);
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles exception when ID provided is invalid.
     *
     * @param ex the exception thrown.
     * @return 404 response with message details..
     */
    @ExceptionHandler(InvalidObjectIdException.class)
    public ResponseEntity<ErrorResponse> handleInvalidObjectIdException(InvalidObjectIdException ex) {
        final String errorMessage = ex.getMessage();
        final ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(errorMessage)
                .build();
        log.warn("Invalid ObjectID: {}", errorMessage);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles exception when duplicate ISBN is used or a Mongo Write exception is thrown.
     *
     * @param ex the exception thrown.
     * @return 409 response with message details.
     */
    @ExceptionHandler({MongoWriteException.class, DuplicateKeyException.class})
    public ResponseEntity<ErrorResponse> handleMongoDbWriteException(Exception ex) {
        final String errorMessage = ex.getMessage();
        ErrorResponse response = ErrorResponse.builder()
                .message(errorMessage)
                .status(HttpStatus.CONFLICT.value())
                .build();
        log.warn("Duplicate ISBN entered: {}", errorMessage);

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * Handles exception when input validation has failed.
     *
     * @param ex the exception thrown.
     * @return 400 response with message details.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        List<FieldError> errors = ex.getBindingResult().getFieldErrors();
        final String errorMessage = "There was an error validating the input.";
        final ValidationErrorResponse errorResponse = ValidationErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(errorMessage)
                .errors(errors.stream()
                        .map(e -> new ValidationError(e.getField(), e.getDefaultMessage()))
                        .toList())
                .build();
        log.warn("Validation errors: {}", errorResponse.getErrors());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles MongoDb Timeout Exception.
     *
     * @param ex the exception thrown.
     * @return 503 response with message details.
     */
    @ExceptionHandler(MongoTimeoutException.class)
    public ResponseEntity<ErrorResponse> handleTimeout(final MongoTimeoutException ex) {
        final String message = ex.getMessage();
        final ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .message(message)
                .build();
        log.error("MongoDB timeout: {}", message);
        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Handles Constraint violations Exception.
     *
     * @param ex the exception thrown.
     * @return 400 response with message details.
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(HandlerMethodValidationException ex) {
        final StringBuilder sb = new StringBuilder("The value %s is not valid.");
        List<ValidationError> errors = ex.getParameterValidationResults().stream()
                .map(error -> new ValidationError(error.getMethodParameter().getParameterName(),
                        String.format(sb.toString(), error.getArgument())))
                .toList();
        final ValidationErrorResponse errorResponse = ValidationErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(ex.getReason())
                .errors(errors)
                .build();
        log.error("Error retrieving paginated books: {}", errors);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles generic exceptions, should not happen.
     *
     * @param ex Exception thrown.
     * @return 500 response with message details.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(final Exception ex) {
        final String message = ex.getMessage();
        final ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(message)
                .build();
        log.error("Unexpected error occurred: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDateException(final HttpMessageNotReadableException ex) {
        String message = ex.getRootCause() instanceof DateTimeParseException e
                ? e.getMessage() : ex.getMessage();
            final ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(message)
                    .build();
            log.error(message);
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingRequestValueException.class)
    public ResponseEntity<ErrorResponse> handleEmptyBody(final MissingRequestValueException ex) {
        final String message = ex.getMessage();
        final ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(message)
                .build();
        log.error("Empty request body: {}.", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
