package fr.tpdd.book.service;

import fr.tpdd.book.Book;
import fr.tpdd.book.Format;
import fr.tpdd.book.NoDataInWebServiceException;
import fr.tpdd.book.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {
    @Mock
    private BookRepository bookRepositoryMock;
    @Mock
    private ISBNValidator isbnValidatorMock;
    @Mock
    private BookWebService webServiceMock;
    @InjectMocks
    private BookService bookService;
    @BeforeEach
    public void init() {
        bookService = new BookService(bookRepositoryMock, isbnValidatorMock, webServiceMock);
    }
    @Test
    public void testGetAllBook(){
        List<Book> existingListOfBooks = new ArrayList<>();
        when(bookRepositoryMock.getAllBook()).thenReturn(existingListOfBooks);
        List<Book> books= bookService.getAll();
        assertEquals(books, existingListOfBooks);
    }
    @Test
    public void testAddBook_Success() {
        Book book = new Book("1234567890", "The Lord of the Rings", "J.R.R. Tolkien", "Houghton Mifflin Harcourt", Format.BROCHE, false);

        when(bookRepositoryMock.save(book)).thenReturn(book);
        when(isbnValidatorMock.validateISBN(book.getIsbn())).thenReturn(true);
        Book addedBook = bookService.addBook(book);

        verify(bookRepositoryMock).save(book);

        assertNotNull(addedBook);
    }
    @Test
    public void testAddBook_DuplicateISBN() {
        String uniqueIsbn = "1234567890";
        Book book = new Book(uniqueIsbn, "The Lord of the Rings", "J.R.R. Tolkien",
                "Houghton Mifflin Harcourt", Format.BROCHE, false);

        when(bookRepositoryMock.findByIsbn(uniqueIsbn)).thenReturn(new Book(uniqueIsbn, "Another Title", "Different Author", "Another Publisher", Format.GRAND_FORMAT, true));

        try {
            bookService.addBook(book);
            fail("Expected exception not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("Duplicate ISBN: " + uniqueIsbn, e.getMessage());
        }

        verify(bookRepositoryMock, never()).save(book);
    }
    @Test
    public void testAddBookRetrieveMissingInformation_Success() {
        Book book = new Book("1234567890", null, null, null, Format.BROCHE, false);

        BookWebService webServiceResponse = new BookWebService(book.getIsbn(), "The Lord of the Rings", "J.R.R. Tolkien", "Houghton Mifflin Harcourt", Format.GRAND_FORMAT, true);
        when(webServiceMock.getBookByISBN(book.getIsbn())).thenReturn(webServiceResponse);
        when(bookRepositoryMock.save(book)).thenReturn(book);
        when(isbnValidatorMock.validateISBN(book.getIsbn())).thenReturn(true);
        Book addedBook = bookService.addBook(book);

        verify(bookRepositoryMock).save(book);
        Book updatedBook = bookService.retrieveMissingInformation(book);

        assertNotNull(updatedBook);
        assertNotNull(addedBook);
        assertEquals(webServiceResponse.getTitle(), updatedBook.getTitle());
        assertEquals(webServiceResponse.getAuthor(), updatedBook.getAuthor());
        assertEquals(webServiceResponse.getPublisher(), updatedBook.getPublisher());
    }
    @Test
    public void testRetrieveMissingInformation_WebServiceNoData() throws Exception {
        Book book = new Book("1234567890", null, null, null, Format.BROCHE, false);
        when(webServiceMock.getBookByISBN(book.getIsbn())).thenReturn(null);
        Book updatedBook = bookService.retrieveMissingInformation(book);
        assertNull(updatedBook);
    }
    @Test
    public void testAddBook_NullBook() {
        bookService.addBook(null);
        verify(bookRepositoryMock, times(0)).save(any(Book.class));
    }

    @Test
    public void testAddBook_InvalidIsbn() {
        Book book = new Book("140274d577X", "Book Title", "Author", "Publisher", Format.GRAND_FORMAT, true);
        bookService.addBook(book);
        verify(bookRepositoryMock, times(0)).save(any(Book.class));
    }
    @Test
    public void testUpdateBook_ExistingBook() {
        Book existingBook = new Book("140274577X", "Book Title", "Author", "Publisher", Format.GRAND_FORMAT, true);
        when(bookRepositoryMock.findByIsbn(existingBook.getIsbn())).thenReturn(existingBook);

        Book updatedBook = new Book(existingBook.getIsbn(), "New Title", "New Author", "New Publisher", Format.BROCHE, true);

        Book resultBook = bookService.updateBook(updatedBook);

        verify(bookRepositoryMock).findByIsbn(existingBook.getIsbn());
        verify(bookRepositoryMock).save(existingBook);

        assertEquals(updatedBook.getTitle(), existingBook.getTitle());
        assertEquals(updatedBook.getAuthor(), existingBook.getAuthor());
        assertTrue(existingBook.isAvailable());
        assertEquals(resultBook, existingBook);
    }

    @Test
    public void testDeleteBook_ExistingBook() {
        Book existingBook = new Book("1234567890", "Title", "Author", "Publisher", Format.BROCHE, true);
        when(bookRepositoryMock.findByIsbn(existingBook.getIsbn())).thenReturn(existingBook);

        bookService.deleteBook(existingBook);

        verify(bookRepositoryMock).findByIsbn(existingBook.getIsbn());
        verify(bookRepositoryMock).delete(existingBook);
    }
    @Test
    public void testDeleteBook_NotExisting() {
        verify(bookRepositoryMock, never()).delete(any(Book.class)); // Not called because book doesn't exist
    }
    @Test
    public void testSearchBook_WithCriteria() {
        Book mockBook = new Book("1234567890", "Book Title", "Author 1", "Publisher 1", Format.GRAND_FORMAT, true);
        when(bookRepositoryMock.findByCriteria("isbn:1234567890 title:Book Title author:Author 1")).thenReturn(mockBook);

        Book searchResults = bookService.searchBook("1234567890", "Book Title", "Author 1");

        verify(bookRepositoryMock).findByCriteria("isbn:1234567890 title:Book Title author:Author 1");

        assertEquals(mockBook, searchResults);
    }
}