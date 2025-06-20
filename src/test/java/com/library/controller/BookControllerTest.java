package com.library.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.dto.BookResponseDTO;
import com.library.dto.CreateBookRequestDTO;
import com.library.dto.PagedBookResponse;
import com.library.dto.UpdateBookRequestDTO;
import com.library.exception.BookNotFoundException;
import com.library.exception.InvalidObjectIdException;
import com.library.service.BookService;
import com.library.util.TestConsts;
import com.mongodb.MongoWriteException;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for the {@link BookController} class.
 */
@SpringBootTest
@AutoConfigureMockMvc
class BookControllerTest {

    /**
     * Base URL path.
     */
    final String BASE_URL = "http://localhost";

    /**
     * The location header value.
     */
    final String LOCATION_HEADER = "Location";

    /**
     * Valid Book JSON to be reused in tests.
     */
    private static final String VALID_BOOK_JSON = """
      {
      "title": "Test Book",
      "author": "Author A",
      "isbn": "9781234567890",
      "publishedDate": "2023-01-01"
    }
    """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Tests the {@link BookController#createBook(CreateBookRequestDTO)} method.
     * Create a valid book, and expect a 201 response.
     */
    @Test
    public void createBook_ShouldReturn201() throws Exception {
        final BookResponseDTO expectedResponse = getBookResponse(VALID_BOOK_JSON);

        Mockito.when(bookService.createBook(any(CreateBookRequestDTO.class)))
                .thenReturn(expectedResponse);

        MockHttpServletResponse response = mockMvc.perform(post(TestConsts.API_BOOKS_PATH)

                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(VALID_BOOK_JSON))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        // Verify correct ID in response URI.
        final String bookLocation = BASE_URL + TestConsts.API_BOOKS_PATH
                + "/" + expectedResponse.getId();
        Assertions.assertEquals(bookLocation, response.getHeader(LOCATION_HEADER),
                "Book ID was invalid, expected " + expectedResponse.getId());

        // Verify the response body.
        final BookResponseDTO actualResponse = objectMapper.readValue(response.getContentAsString(),
                BookResponseDTO.class);
        Assertions.assertEquals(expectedResponse, actualResponse,
                "The expected book response was: " + expectedResponse);
    }

    /**
     * Tests the {@link BookController#createBook(CreateBookRequestDTO)} method.
     * Invalid create request should return a 400 response.
     */
    @Test
    public void createBook_InvalidRequest_ShouldReturn400() throws Exception {
        final String invalidJsonRequest = """
                {
                  "author": "me"
                }
                """;

        // Use null request.
        String response = mockMvc.perform(post(TestConsts.API_BOOKS_PATH)

                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(invalidJsonRequest))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        Assertions.assertTrue(response.contains("There was an error validating the input"));
    }

    /**
     * Tests the {@link BookController#createBook(CreateBookRequestDTO)} method.
     * Invalid create request should return a 400 response.
     */
    @Test
    public void createBook_NullRequest_ShouldReturn400() throws Exception {
        // Use null request.
        MockHttpServletResponse response = mockMvc.perform(post(TestConsts.API_BOOKS_PATH)

                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(""))
                .andReturn().getResponse();

        // Verify 400 status.
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
    }

    /**
     * Tests the {@link BookController#createBook(CreateBookRequestDTO)} method.
     * Create book with duplicate ISBN should fail with 409.
     */
    @Test
    void createBook_WithDuplicateISBN_ShouldReturn409Conflict() throws Exception {
        // Mock the exception.
        DuplicateKeyException exception = mock(DuplicateKeyException.class);
        Mockito.when(bookService.createBook(any(CreateBookRequestDTO.class)))
                .thenThrow(exception);

        MockHttpServletResponse response = mockMvc.perform(post(TestConsts.API_BOOKS_PATH)

                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(VALID_BOOK_JSON))
                .andReturn().getResponse();

        // Verify 409 status.
        Assertions.assertEquals(HttpStatus.CONFLICT.value(), response.getStatus());
    }

    /**
     * Tests the {@link BookController#getBookById(String)} method.
     * Retrieves book with ID, 200 response.
     */
    @Test
    void getBookById_ShouldReturn200AndBook() throws Exception {
        final String bookId = "665a22d92ff54c05e8db1234";
        final BookResponseDTO expectedResponse = getBookResponse(VALID_BOOK_JSON);

        // Mock response.
        Mockito.when(bookService.getBookById(any()))
                .thenReturn(expectedResponse);

        // Call API
        MockHttpServletResponse response = mockMvc.perform(get(TestConsts.API_BOOKS_PATH
                        + "/" + bookId))
                .andReturn().getResponse();

        // Verify status code.
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());

        // Verify the response body.
        final BookResponseDTO actualResponse = objectMapper.readValue(response.getContentAsString(),
                BookResponseDTO.class);
        Assertions.assertEquals(expectedResponse, actualResponse,
                "The expected book response was: " + expectedResponse);
    }

    /**
     * Tests the {@link BookController#getBookById(String)} method.
     * Retrieve book with ID not used should return 404.
     */
    @Test
    void getBookById_NotFound_ShouldReturn404() throws Exception {
        final String bookId = "665a22d92ff54c05e8db1234";

        // Mock response
        doThrow(new BookNotFoundException(bookId))
                .when(bookService).getBookById(bookId);

        // Call API.
        MockHttpServletResponse response = mockMvc.perform(get(TestConsts.API_BOOKS_PATH
                        + "/" + bookId))
                .andReturn().getResponse();

        // Verify status code.
        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());

        // Verify message.
        Assertions.assertTrue(response.getContentAsString()
                .contains("Book with ID: " + bookId + " was not found"));
    }

    /**
     * Tests the {@link BookController#getBookById(String)} method.
     * Retrieve book with ID not used should return 404.
     */
    @Test
    void getBookById_InvalidId_ShouldReturn400() throws Exception {
        final String bookId = "1";

        // Mock response.
        doThrow(new InvalidObjectIdException(bookId))
                .when(bookService).getBookById(bookId);

        // Call API.
        MockHttpServletResponse response = mockMvc.perform(get(TestConsts.API_BOOKS_PATH
                        + "/" + bookId))
                .andReturn().getResponse();

        // Verify status code.
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());

        // Verify status message.
        Assertions.assertTrue(response.getContentAsString()
                .contains("The ID: " + bookId + " is not valid"));
    }

    /**
     * Tests the {@link BookController#getAllBooks(int, int)} method.
     * Should return 200 and {@link com.library.dto.PagedBookResponse} list.
     */
    @Test
    void getAllBooks_ShouldReturnPagedList() throws Exception {
        List<BookResponseDTO> books = List.of(
                BookResponseDTO.builder()
                        .id("1")
                        .title("Book One")
                        .author("Author One")
                        .isbn("1234567890123")
                        .publishedDate("2024-02-02")
                        .build(),
                BookResponseDTO.builder()
                        .id("2")
                        .title("Book Two")
                        .author("Author Two")
                        .isbn("1234567890124")
                        .publishedDate("1923-12-12")
                        .build()
        );
        Page<BookResponseDTO> page = new PageImpl<>(books);
        Mockito.when(bookService.getAllBooks(0, 10))
                .thenReturn(page);

        MockHttpServletResponse response = mockMvc.perform(get(TestConsts.API_BOOKS_PATH)
                )
                .andReturn().getResponse();

        // Verify Response status.
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());

        PagedBookResponse actualPage
                = objectMapper.readValue(response.getContentAsString(), PagedBookResponse.class);
        Assertions.assertEquals(PagedBookResponse.from(page), actualPage);
    }

    /**
     * Tests the {@link BookController#getAllBooks(int, int)} method.
     * Invalid page number should return 400.
     */
    @Test
    void getAllBooks_NegativePage_ShouldReturn400() throws Exception {
        String pageParam = "page";
        String pageValue = "-2";
        MockHttpServletResponse response = mockMvc.perform(get(TestConsts.API_BOOKS_PATH)

                        .param(pageParam, pageValue)
                        .param("size", "10"))
                .andReturn().getResponse();

        // Verify Response status.
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());

        // Verify Message.
        String errorMessage = response.getContentAsString();

        Assertions.assertTrue(errorMessage.contains(pageParam));
        Assertions.assertTrue(errorMessage.contains("The value " + pageValue + " is not valid"));
    }

    /**
     * Tests the {@link BookController#updateBook(String, UpdateBookRequestDTO)} method.
     * Returns 200 and updated book.
     */
    @Test
    void updateBook_ShouldReturn200AndUpdatedBook() throws Exception {
        String bookId = "665a22d92ff54c05e8db1234";
        UpdateBookRequestDTO updateRequest = UpdateBookRequestDTO.builder()
                .title("Updated title")
                .author("Updated author")
                .isbn("1234567890123")
                .publishedDate(LocalDate.MIN)
                .build();

        BookResponseDTO updatedResponse = BookResponseDTO.builder()
                .id(bookId)
                .title(updateRequest.getTitle())
                .author(updateRequest.getAuthor())
                .isbn(updateRequest.getIsbn())
                .publishedDate(updateRequest.getPublishedDate().toString())
                .build();

        when(bookService.updateBook(eq(bookId), any(UpdateBookRequestDTO.class)))
                .thenReturn(updatedResponse);

        MockHttpServletResponse response = mockMvc.perform(post(TestConsts.API_BOOKS_PATH
                        + "/" + bookId)
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andReturn().getResponse();

        // Verify 200 Status code.
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());

        BookResponseDTO actualResponse = objectMapper.readValue(response.getContentAsString(), BookResponseDTO.class);
        Assertions.assertEquals(updatedResponse, actualResponse);
    }

    /**
     * Tests the {@link BookController#updateBook(String, UpdateBookRequestDTO)} method.
     * Book ID not found, should return 404.
     */
    @Test
    void updateBook_NotFound_ShouldReturn404() throws Exception {
        String bookId = "665a22d92ff54c05e8db9999";
        UpdateBookRequestDTO updateRequest = UpdateBookRequestDTO.builder()
                .title("Updated Title")
                .author("Updated Author")
                .isbn("1234567890123")
                .publishedDate(LocalDate.of(2024, 1, 1))
                .build();

        // Simulate the book not being found
        when(bookService.updateBook(eq(bookId), any(UpdateBookRequestDTO.class)))
                .thenThrow(new BookNotFoundException(bookId));

        MockHttpServletResponse response = mockMvc.perform(post(TestConsts.API_BOOKS_PATH
                        + "/" + bookId)
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andReturn().getResponse();

        // Verify status.
        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());

        // Verify message.
        Assertions.assertTrue(response.getContentAsString()
                .contains("Book with ID: " + bookId + " was not found"));
    }

    /**
     * Tests the {@link BookController#updateBook(String, UpdateBookRequestDTO)} method.
     * Bad input data should return 400.
     */
    @Test
    void updateBook_WithInvalidData_ShouldReturn400() throws Exception {
        String bookId = "2";
        UpdateBookRequestDTO updateRequest = UpdateBookRequestDTO.builder()
                .title("Updated Title")
                .author("Updated Author")
                .isbn("1234567890123")
                .publishedDate(LocalDate.of(2024, 1, 1))
                .build();

        // Mock update request contains bad data.
        when(bookService.updateBook(eq(bookId), any(UpdateBookRequestDTO.class)))
                .thenThrow(new InvalidObjectIdException(bookId));

        MockHttpServletResponse response = mockMvc.perform(post(TestConsts.API_BOOKS_PATH
                        + "/" + bookId)
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andReturn().getResponse();

        // Verify status.
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());

        // Verify message.
        Assertions.assertTrue(response.getContentAsString()
                .contains("The ID: " + bookId + " is not valid"));
    }

    /**
     * Tests the {@link BookController#updateBook(String, UpdateBookRequestDTO)} method.
     * Duplicate ISBN should return 409.
     */
    @Test
    void updateBook_WithDuplicateIsbn_ShouldReturn409() throws Exception {
        String bookId = "665a22d92ff54c05e8db9999";
        UpdateBookRequestDTO updateRequest = UpdateBookRequestDTO.builder()
                .isbn("1234567890123")
                .build();

        // Mock the exception.
        MongoWriteException exception = mock(MongoWriteException.class);

        // Simulate the book not being found
        when(bookService.updateBook(eq(bookId), any(UpdateBookRequestDTO.class)))
                .thenThrow(exception);

        MockHttpServletResponse response = mockMvc.perform(post(TestConsts.API_BOOKS_PATH
                        + "/" + bookId)
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andReturn().getResponse();

        // Verify status.
        Assertions.assertEquals(HttpStatus.CONFLICT.value(), response.getStatus());
    }

    /**
     * Tests the {@link BookController#deleteBook(String)} method.
     * Deleted book should return 204.
     */
    @Test
    void deleteBook_ShouldReturn204NoContent() throws Exception{
        String bookId = "665a22d92ff54c05e8db9999";

        // Call delete book request.
        MockHttpServletResponse response = mockMvc.perform(delete(TestConsts.API_BOOKS_PATH
                        + "/" + bookId))
                .andReturn().getResponse();

        Assertions.assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatus());
    }

    /**
     * Tests the {@link BookController#deleteBook(String)} method.
     * Book not found should return 404.
     */
    @Test
    void deleteBook_NotFound_ShouldReturn404() throws Exception {
        String bookId = "665a22d92ff54c05e8db9999";

        // Mock not found exception.
        doThrow(new BookNotFoundException(bookId))
                .when(bookService).deleteBook(bookId);

        // Call delete book request.
        MockHttpServletResponse response = mockMvc.perform(delete(TestConsts.API_BOOKS_PATH
                        + "/" + bookId))
                .andReturn().getResponse();

        // Verify status
        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());

        // Verify message.
        Assertions.assertTrue(response.getContentAsString()
                .contains("Book with ID: " + bookId + " was not found"));
    }

    /**
     * Tests the {@link BookController#deleteBook(String)} method.
     * Invalid ID should return 400.
     */
    @Test
    void deleteBook_InvalidId_ShouldReturn400() throws Exception {
        String bookId = "1";

        // Mock not found exception.
        doThrow(new InvalidObjectIdException(bookId))
                .when(bookService).deleteBook(bookId);

        // Call delete book request.
        MockHttpServletResponse response = mockMvc.perform(delete(TestConsts.API_BOOKS_PATH
                        + "/" + bookId))
                .andReturn().getResponse();

        // Verify status
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());

        // Verify message.
        Assertions.assertTrue(response.getContentAsString()
                .contains("The ID: " + bookId + " is not valid"));
    }

    /**
     * Helper method to create a BookResponse given a create request.
     *
     * @param requestJson the json string representing the book.
     * @return the {@link BookResponseDTO}.
     */
    private BookResponseDTO getBookResponse(final String requestJson) throws JsonProcessingException {
        final CreateBookRequestDTO request
                = objectMapper.readValue(requestJson, CreateBookRequestDTO.class);
        return BookResponseDTO.builder()
                .id(new ObjectId().toString())
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .publishedDate(request.getPublishedDate().toString())
                .build();
    }
}
