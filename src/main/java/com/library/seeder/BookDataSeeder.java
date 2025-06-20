package com.library.seeder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.dto.BookMapper;
import com.library.dto.CreateBookRequestDTO;
import com.library.model.Book;
import com.library.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

/**
 * Class that reads and adds seed data to books db.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
public class BookDataSeeder implements CommandLineRunner {

    private final BookRepository bookRepository;
    private final ObjectMapper objectMapper;
    private final BookMapper bookMapper;

    /**
     * Checks for existing data and adds seeds data if books db is empty.
     *
     * @param args incoming main method arguments
     * @throws Exception thrown if file is not found or cannot be read.
     */
    @Override
    public void run(String... args) throws Exception {

        // Uncomment the following line to clear old data and add seed data.
        // bookRepository.deleteAll();
        final long booksCount = bookRepository.count();
        if (booksCount != 0) {
            log.debug("Book is already initialized with {} documents.", booksCount);
        } else {
            InputStream inputStream = getClass()
                    .getClassLoader()
                    .getResourceAsStream("books.json");

            if (inputStream == null) {
                log.error("Could not find books.json file.");
                throw new FileNotFoundException("books.json was not found.");
            }

            List<CreateBookRequestDTO> requests = objectMapper.readValue(inputStream,
                    new TypeReference<List<CreateBookRequestDTO>>() {});
            List<Book> books = bookMapper.toEntityList(requests);
            bookRepository.saveAll(books);

            log.info("Added {} books.", books.size());
        }
    }
}
