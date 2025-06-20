package com.library.dto;

import com.library.model.Book;
import org.mapstruct.*;

import java.util.List;

/**
 * Book Mapper for transforming between request/response DTOs and entity.
 */
@Mapper(componentModel = "spring", uses = Book.class)
public interface BookMapper {

    /**
     * Converts a {@link CreateBookRequestDTO} to {@link Book}.
     *
     * @param request the request dto to convert.
     * @return the corresponding book.
     */
    @Mapping(target = "id", ignore = true)
    Book toEntity(final CreateBookRequestDTO request);

    /**
     * Converts a {@link Book} to {@link BookResponseDTO}.
     *
     * @param book the book entity to convert.
     * @return the corresponding book response.
     */
    @Mapping(target = "publishedDate", dateFormat = "dd-MM-yyyy")
    BookResponseDTO toResponse(final Book book);

    /**
     * Updates an existing {@link Book} entity using the data from the {@link UpdateBookRequestDTO}.
     * @param request the request containing the new book data.
     * @param book the book entity to be updated.
     * @return the updated book entity.
     */
    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Book updateEntity(final UpdateBookRequestDTO request, @MappingTarget Book book);

    /**
     * Creates a list of {@link Book} using a list of {@link CreateBookRequestDTO}.
     *
     * @param bookRequestList the list containing book data.
     * @return list of book entities.
     */
    @Mapping(target = "id", ignore = true)
    List<Book> toEntityList(List<CreateBookRequestDTO> bookRequestList);
}
