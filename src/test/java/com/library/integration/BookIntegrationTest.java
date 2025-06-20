package com.library.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.dto.BookResponseDTO;
import com.library.dto.CreateBookRequestDTO;
import com.library.dto.PagedBookResponse;
import com.library.dto.UpdateBookRequestDTO;
import com.library.repository.BookRepository;
import com.library.util.TestConsts;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integrated test using test containers.
 */
@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
@Testcontainers
public class BookIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

    @DynamicPropertySource
    static void overrideMongoDbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        System.out.println(mongoDBContainer.getReplicaSetUrl());
    }

    private CreateBookRequestDTO createBookRequestDTO;

    private UpdateBookRequestDTO updateBookRequestDTO;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();

        createBookRequestDTO = CreateBookRequestDTO.builder()
                .title("Integration Test")
                .author("Author Name")
                .isbn("9876543210123")
                .publishedDate(LocalDate.of(2024, 6, 1))
                .build();

        updateBookRequestDTO = UpdateBookRequestDTO.builder()
                .title("Integration Test")
                .author("Author Name")
                .isbn("9876543210123")
                .publishedDate(LocalDate.of(2024, 6, 1))
                .build();
    }

    /**
     * Tests creating and retrieving a book.
     * Should return response codes 201, then 200.
     */
    @Test
    void createAndGetBook_ShouldSucceed() throws Exception {
        // Create
        String responseJson = mockMvc.perform(post(TestConsts.API_BOOKS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBookRequestDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        BookResponseDTO created = objectMapper.readValue(responseJson, BookResponseDTO.class);

        // Get
        String getResponseJson = mockMvc.perform(get(TestConsts.API_BOOKS_PATH + "/" + created.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        BookResponseDTO retrieved = objectMapper.readValue(getResponseJson, BookResponseDTO.class);

        Assertions.assertEquals(created, retrieved);
    }

    @Test
    void getBookById_NotFound_ShouldReturn404() throws Exception {
        String bookId = "665b11112222333344445555";

        String responseJson = mockMvc.perform(get(TestConsts.API_BOOKS_PATH + "/" + bookId))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assertions.assertTrue(responseJson
                .contains("Book with ID: " + bookId + " was not found"));
    }

    /**
     * Test that creates a book, deletes it, then attempts to retrieve it.
     * Should expect response status of 201, 204, then 404.
     */
    @Test
    void deleteBook_ShouldRemoveBook() throws Exception {
        // Create a book.
        String responseJson = mockMvc.perform(post(TestConsts.API_BOOKS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBookRequestDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        BookResponseDTO created = objectMapper.readValue(responseJson, BookResponseDTO.class);

        // Retrieve book ID.
        final String bookId = created.getId();

        // Delete book.
        mockMvc.perform(delete(TestConsts.API_BOOKS_PATH + "/" + bookId))
                .andExpect(status().isNoContent());

        // Get book by ID, expect 404.
        String notFoundJson = mockMvc.perform(get(TestConsts.API_BOOKS_PATH + "/" + bookId))
                        .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assertions.assertTrue(notFoundJson.contains("Book with ID: " + bookId + " was not found"));
    }

    /**
     * Verify paginated response of all books.
     */
    @Test
    void getAllBooks_ShouldReturnPagedList() throws Exception{
        // Add 3 books.
        CreateBookRequestDTO book1 = CreateBookRequestDTO.builder()
                .title("Book One")
                .author("Author A")
                .isbn("1000000000001")
                .publishedDate(LocalDate.of(2020, 1, 1))
                .build();

        CreateBookRequestDTO book2 = CreateBookRequestDTO.builder()
                .title("Book Two")
                .author("Author B")
                .isbn("1000000000002")
                .publishedDate(LocalDate.of(2021, 2, 2))
                .build();

        CreateBookRequestDTO book3 = CreateBookRequestDTO.builder()
                .title("Book Three")
                .author("Author C")
                .isbn("1000000000003")
                .publishedDate(LocalDate.of(2022, 3, 3))
                .build();

        mockMvc.perform(post(TestConsts.API_BOOKS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(TestConsts.API_BOOKS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book2)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(TestConsts.API_BOOKS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book3)))
                .andExpect(status().isCreated());

        // Get all books, paginated.
        String responseJson = mockMvc.perform(get(TestConsts.API_BOOKS_PATH)
                        .param("page", "0")
                        .param("size", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PagedBookResponse response = objectMapper.readValue(responseJson, PagedBookResponse.class);
        Assertions.assertEquals(0, response.currentPage());
        Assertions.assertEquals(3, response.totalBooks());
        Assertions.assertEquals(2, response.totalPages());
    }

    /**
     * Test that creates a book, retrieves it, then updates it.
     * Book should be updated.
     */
    @Test
    void updateBook_ShouldUpdateFields() throws Exception {
        // Create
        String responseJson = mockMvc.perform(post(TestConsts.API_BOOKS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBookRequestDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        BookResponseDTO savedBook = objectMapper.readValue(responseJson, BookResponseDTO.class);

        String newTitle = "Updated Title";
        String newAuthor = "Updated Author";
        UpdateBookRequestDTO updateBookRequestDTO = UpdateBookRequestDTO.builder()
                .title(newTitle)
                .author(newAuthor)
                .build();

        String updateJson = mockMvc.perform(post(TestConsts.API_BOOKS_PATH
                        + "/" + savedBook.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateBookRequestDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        BookResponseDTO updatedBook = objectMapper.readValue(updateJson, BookResponseDTO.class);
        Assertions.assertEquals(newAuthor, updatedBook.getAuthor());
        Assertions.assertEquals(newTitle, updatedBook.getTitle());
        Assertions.assertEquals(createBookRequestDTO.getIsbn(), updatedBook.getIsbn());
        Assertions.assertEquals(createBookRequestDTO.getPublishedDate(),
                LocalDate.parse(updatedBook.getPublishedDate(), TestConsts.DATE_FORMATTER));
        Assertions.assertEquals(savedBook.getId(), updatedBook.getId());
    }

    /**
     * Test creating a book with missing fields.
     * Should return 400.
     */
    @Test
    void createBook_MissingFields_ShouldReturn400() throws Exception {
        CreateBookRequestDTO book = CreateBookRequestDTO.builder()
                .title("Book One")
                .author("Author A")
                .publishedDate(LocalDate.of(2020, 1, 1))
                .build();

        // Create invalid request.
        String responseJson = mockMvc.perform(post(TestConsts.API_BOOKS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        Assertions.assertTrue(responseJson.contains("There was an error validating the input."));
        Assertions.assertTrue(responseJson.contains("ISBN is required."));
    }

    @Test
    void createBook_DuplicateISBN_ShouldReturn409() throws Exception {
        mockMvc.perform(post(TestConsts.API_BOOKS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBookRequestDTO)))
                .andExpect(status().isCreated());

        CreateBookRequestDTO duplicateIsbnBook = CreateBookRequestDTO.builder()
                .title("Book One")
                .author("Author A")
                .isbn(createBookRequestDTO.getIsbn())
                .publishedDate(LocalDate.of(2020, 12, 22))
                .build();

        String responseJson = mockMvc.perform(post(TestConsts.API_BOOKS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateIsbnBook)))
                .andExpect(status().isConflict())
                .andReturn().getResponse().getContentAsString();

        Assertions.assertTrue(responseJson.contains("duplicate key error"));
    }

    @Test
    void updateBook_InvalidId_ShouldReturn400() throws Exception{
        String invalidId = "1";

        String responseJson = mockMvc.perform(post(TestConsts.API_BOOKS_PATH + "/" + invalidId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateBookRequestDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        Assertions.assertTrue(responseJson.contains("The ID: " + invalidId + " is not valid"));
    }

    @Test
    void getAllBooks_NegativePage_ShouldReturn400() throws Exception{
        String responseJson = mockMvc.perform(get(TestConsts.API_BOOKS_PATH)
                        .param("page", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        Assertions.assertTrue(responseJson.contains("Validation failure"));
        Assertions.assertTrue(responseJson.contains("\"parameter\":\"page\""));
        Assertions.assertTrue(responseJson.contains("The value -1 is not valid."));
    }

    @Test
    void getAllBooks_EmptyDatabase_ShouldReturnEmptyList() throws Exception{
        String responseJson = mockMvc.perform(get(TestConsts.API_BOOKS_PATH)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PagedBookResponse response = objectMapper.readValue(responseJson, PagedBookResponse.class);
        Assertions.assertEquals(0, response.totalBooks());
        Assertions.assertEquals(0, response.currentPage());
        Assertions.assertEquals(0, response.totalPages());
        Assertions.assertTrue(response.books().isEmpty());
    }

}
