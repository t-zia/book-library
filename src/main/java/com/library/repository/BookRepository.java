package com.library.repository;

import com.library.model.Book;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for {@link Book} entities.
 */
@Repository
public interface BookRepository extends MongoRepository<Book, ObjectId> {
}
