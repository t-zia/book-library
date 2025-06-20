package com.library.service;

import com.library.dto.BookMapper;
import com.library.dto.BookResponseDTO;
import com.library.dto.CreateBookRequestDTO;
import com.library.dto.UpdateBookRequestDTO;
import com.library.exception.BookNotFoundException;
import com.library.exception.InvalidObjectIdException;
import com.library.model.Book;
import com.library.repository.BookRepository;
import com.mongodb.MongoWriteException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 * Service layer to carry out business logic of CRUD APIs.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private final BookRepository bookRepository;

    private final BookMapper bookMapper;

    /**
     * Method to create and persist a book entity.
     *
     * @param request the {@link CreateBookRequestDTO} containing the book details.
     * @return the {@link BookResponseDTO}.
     * @throws MongoWriteException ISBN in provided request already exists.
     */
    public BookResponseDTO createBook(CreateBookRequestDTO request) throws MongoWriteException {
        final Book book = bookMapper.toEntity(request);
        bookRepository.save(book);
        log.info("Creating new book with ID: {}", book.getId());
        return bookMapper.toResponse(book);
    }

    /**
     * Method to retrieve all books in the repository.
     *
     * @param page zero-based page number of the books.
     * @param size the number of books to display per page.
     * @return {@link Page} of books in the repository.
     */
    public Page<BookResponseDTO> getAllBooks(int page, int size) {
        Page<Book> booksPage = bookRepository.findAll(PageRequest.of(page, size));
        log.debug("Retrievd {} books on page {}.", size, page);
        return booksPage.map(bookMapper::toResponse);
    }

    /**
     * Updates a {@link Book}.
     *
     * @param id the ObjectId of the book to update.
     * @param request the data of the book to update.
     * @return the {@link BookResponseDTO}.
     * @throws MongoWriteException ISBN in provided request already exists.
     */
    public BookResponseDTO updateBook(final String id, final UpdateBookRequestDTO request)
            throws MongoWriteException {
        Book book = validateBook(id);

        Book updatedBook = bookMapper.updateEntity(request, book);

        bookRepository.save(updatedBook);
        log.info("Updated book with ID: {}, updated: {}.", book.getId(), request.toString());
        return bookMapper.toResponse(updatedBook);
    }

    /**
     * Get a {@link Book} by ID.
     *
     * @param id the ObjectId of the book.
     * @return the {@link BookResponseDTO}.
     */
    public BookResponseDTO getBookById(final String id) {
        Book book = validateBook(id);

        log.info("Found book with ID: {}.", book.getId());
        return bookMapper.toResponse(book);
    }

    /**
     * Deletes a book for the given ID.
     *
     * @param id the ObjectId of the book.
     */
    public void deleteBook(final String id) {
        Book book = validateBook(id);

        bookRepository.delete(book);
        log.info("Deleted book with ID: {}.", book.getId());
    }

    /**
     * Helper method to validate the given Id.
     * @param id the Object ID of the book.
     * @return the Book for the given ID.
     * @throws InvalidObjectIdException Object Id is not valid.
     * @throws BookNotFoundException No book was found for the given ID.
     */
    private Book validateBook(final String id) throws InvalidObjectIdException, BookNotFoundException{
        if (!ObjectId.isValid(id)) {
            throw new InvalidObjectIdException(id);
        }

        return bookRepository.findById(new ObjectId(id))
                .orElseThrow(() -> new BookNotFoundException(id));
    }
}
