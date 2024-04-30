package fr.tpdd.book.repository;

import fr.tpdd.book.Book;

import java.util.List;

public interface BookRepository  {
    Book findByIsbn(String isbn);

    Book save(Book book);

    Book delete(Book book);

    Book findByCriteria(String criteria);
    List<Book> getAllBook();
}
