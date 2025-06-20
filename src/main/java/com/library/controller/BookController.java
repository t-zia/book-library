package com.library.controller;

import com.library.dto.*;
import com.library.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * REST controller for managing books.
 *
 * <p>Exposed endpoints:
 * <ul>
 *     <li>POST /books - Creates a new book.</li>
 *     <li>GET /books - Lists all books (paginated).</li>
 *     <li>POST /books/{id} - Updates a book.</li>
 *     <li>DELETE /books/{id} - Deletes a book.</li>
 * </ul>
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /**
     * Creates a new book.
     * @param request the {@link CreateBookRequestDTO}.
     * @return the {@link BookResponseDTO}.
     */
    @PostMapping
    @Operation(
            summary = "Add a new book.", description = "Adds a book to the database.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Book created successfully.",
                            content = @Content(schema = @Schema(implementation = BookResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input.",
                            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Duplicate ISBN entered.",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            }
    )
    public ResponseEntity<BookResponseDTO> createBook(@Valid @RequestBody CreateBookRequestDTO request) {
        log.debug("Create book request with data: {}", request);
        final BookResponseDTO bookResponseDTO = bookService.createBook(request);
        final URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(bookResponseDTO.getId())
                .toUri();
        log.info("Successfully created book {}.", bookResponseDTO);
        return ResponseEntity.created(location).body(bookResponseDTO);
    }

    /**
     * Retrieves a paginated list of books.
     *
     * @param page The page number to retrieve (0-indexed).
     * @param size The number of books per page.
     * @return the {@link PagedBookResponse}.
     */
    @GetMapping
    @Operation(
            summary = "Retrieves all books.", description = "Retrieves all books in the db.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of all books."),
                    @ApiResponse(responseCode = "400", description = "Invalid input.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public ResponseEntity<PagedBookResponse> getAllBooks(@RequestParam(defaultValue = "0") @Min(0) final int page,
    @RequestParam(defaultValue = "10") @Min(0) final int size) {
        log.debug("Get all book request with page {} and size {}.", page, size);
        Page<BookResponseDTO> bookResponses = bookService.getAllBooks(page, size);
        return ResponseEntity.ok(PagedBookResponse.from(bookResponses));
    }

    /**
     * Retrieves a book by the ID.
     * @param id the ID of the book to retrieve.
     * @return the {@link BookResponseDTO}.
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Retrieves the book for a given book ID.",
            description = "Retrieves the book in the DB given the ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Retrieved book.",
                            content = @Content(schema = @Schema(implementation = BookResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input.",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Book not found.",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public ResponseEntity<BookResponseDTO> getBookById(@PathVariable String id) {
        log.debug("Retrieve book for ID: {}.", id);
        final BookResponseDTO response = bookService.getBookById(id);
        log.info("Book retrieved successfully: {}.", response);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates a book.
     * @param id the ID of the book to update.
     * @param request the {@link UpdateBookRequestDTO}
     * @return the {@link BookResponseDTO}.
     */
    @PostMapping("/{id}")
    @Operation(
            summary = "Updates the book for a given book ID.",
            description = "Updates the book in the DB given the ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Updated book."),
                    @ApiResponse(responseCode = "400", description = "Invalid input.",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Book not found.",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Duplicate ISBN entered.",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public ResponseEntity<BookResponseDTO> updateBook(@PathVariable String id,
                                                      @Valid @RequestBody UpdateBookRequestDTO request) {
        log.debug("Update book request for ID: {} with body: {}", id, request);
        final BookResponseDTO response = bookService.updateBook(id, request);
        log.info("Book updated successfully: {}.", response);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a book.
     *
     * @param id the ID of the book to delete.
     * @return the {@link ResponseEntity}.
     */
    @DeleteMapping("{id}")
    @Operation(
            summary = "Deletes the book for a given book ID.",
            description = "Deletes the book in the DB given the ID.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Book deleted."),
                    @ApiResponse(responseCode = "400", description = "Invalid input.",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Book not found.",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public ResponseEntity<Void> deleteBook(@PathVariable String id) {
        log.info("Delete book request for book with ID: {}.", id);
        bookService.deleteBook(id);
        log.info("Deleted book with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}
