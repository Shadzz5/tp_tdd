package fr.tpdd.book.service;

import fr.tpdd.book.Book;
import fr.tpdd.book.NoDataInWebServiceException;
import fr.tpdd.book.repository.BookRepository;

import java.util.ArrayList;
import java.util.List;

public class BookService {

    private final BookRepository bookRepository;
    private  final ISBNValidator isbnValidator;
    private BookWebService webServiceResponse;
    public BookService(BookRepository bookRepository, ISBNValidator isbnValidator, BookWebService webServiceResponse) {
        this.bookRepository = bookRepository;
        this.isbnValidator = isbnValidator;
        this.webServiceResponse = webServiceResponse;
    }
    public boolean bookIsValid(Book book) {
        return this.isbnValidator.validateISBN(book.getIsbn());
    }

    public Book addBook(Book book) {
        if (book != null) {
            // Check for existing book with same ISBN
            Book existingBook = bookRepository.findByIsbn(book.getIsbn());
            if (existingBook != null) {
                throw new IllegalArgumentException("Duplicate ISBN: " + book.getIsbn());
            }

            // Validate other book properties and handle missing information
            if (book.getTitle() == null || book.getAuthor() == null || book.getPublisher() == null) {
                if (bookIsValid(book)) {
                    bookRepository.save(retrieveMissingInformation(book));
                    return retrieveMissingInformation(book);
                } else {
                    throw new IllegalArgumentException("ISBN not valid or missing information");
                }
            }

            // If no issues, validate and save the book
            if (bookIsValid(book)) {
                bookRepository.save(book);
                return book;
            } else {
                return null; // Or throw an exception for invalid book content
            }
        }
        return null;
    }

    public Book updateBook(Book book) {
        if (book == null || book.getIsbn() == null || book.getIsbn().isEmpty()) {
            throw new IllegalArgumentException("Book object or ISBN cannot be null or empty");
        }

        Book existingBook = bookRepository.findByIsbn(book.getIsbn());
        if (existingBook == null) {
            throw new IllegalArgumentException("Book with ISBN " + book.getIsbn() + " not found");
        }

        existingBook.setTitle(book.getTitle());
        existingBook.setAuthor(book.getAuthor());
        existingBook.setPublisher(book.getPublisher());
        existingBook.setFormat(book.getFormat());
        existingBook.setAvailable(book.isAvailable());

        bookRepository.save(existingBook);

        return existingBook;
    }
    public void deleteBook(Book book) {
        if (book == null || book.getIsbn() == null || book.getIsbn().isEmpty()) {
            throw new IllegalArgumentException("Book object or ISBN cannot be null or empty");
        }

        Book existingBook = bookRepository.findByIsbn(book.getIsbn());
        if (existingBook == null) {
            throw new IllegalArgumentException("Book with ISBN " + book.getIsbn() + " not found");
        }

        bookRepository.delete(existingBook);
    }
    public Book searchBook(String isbn, String title, String author) {

        String searchCriteria = "";
        Book searchResults = null;
        if (isbn != null && !isbn.isEmpty()) {
            searchCriteria += "isbn:" + isbn + " ";
        }
        if (title != null && !title.isEmpty()) {
            searchCriteria += "title:" + title + " ";
        }
        if (author != null && !author.isEmpty()) {
            searchCriteria += "author:" + author + " ";
        }

        // Remove trailing space if any criteria were added
        searchCriteria = searchCriteria.trim();

        if (!searchCriteria.isEmpty()) {
            searchResults = bookRepository.findByCriteria(searchCriteria);
        }

        return searchResults;
    }

    public Book retrieveMissingInformation(Book book) { // Consider adding exception for web service errors
        // Check if any fields are missing
        if (book.getTitle() == null || book.getAuthor() == null || book.getPublisher() == null) {
            // Retrieve book information from web service using ISBN from the book
            Book webService = webServiceResponse.getBookByISBN(book.getIsbn());

            if(webService != null){
                // Update book fields with web service data
                book.setTitle(webService.getTitle());
                book.setAuthor(webService.getAuthor());
                book.setPublisher(webService.getPublisher());
            }
            else{
                return null;
            }

        }

        return book;
    }
    public List<Book> getAll(){
        return bookRepository.getAllBook();
    }

}
