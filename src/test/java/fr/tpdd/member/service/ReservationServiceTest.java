package fr.tpdd.member.service;

import fr.tpdd.book.Book;
import fr.tpdd.book.Format;
import fr.tpdd.book.repository.BookRepository;
import fr.tpdd.book.service.BookService;
import fr.tpdd.member.MaritalStatus;
import fr.tpdd.member.Member;
import fr.tpdd.member.Reservation;
import fr.tpdd.member.ReservationStatus;
import fr.tpdd.member.repository.MemberRepository;
import fr.tpdd.member.repository.ReservationRepository;
import fr.tpdd.member.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservationServiceTest {

    private ReservationService reservationService;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BookService bookService;

    @Mock
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        reservationService = new ReservationService(reservationRepository, memberRepository, bookService, bookRepository);
    }

    @Test
    void testCheckAvailability_BookNotFound() {
        when(bookRepository.findByIsbn(anyString())).thenReturn(null);
        assertFalse(reservationService.checkAvailability("ISBN123", LocalDate.now()));
    }

    @Test
    void testCheckAvailability_ExistingReservation() {
        when(bookRepository.findByIsbn(anyString())).thenReturn(new Book("1234567890", "The Lord of the Rings", "J.R.R. Tolkien", "Houghton Mifflin Harcourt", Format.BROCHE, false));
        when(reservationRepository.findByBookAndReservationDate(any(), any())).thenReturn(new Reservation(4L,
                new Book("1234567890", "The Lord of the Rings", "J.R.R. Tolkien", "Houghton Mifflin Harcourt", Format.BROCHE, false),
                new Member(8L, "5", "Qu", "Kev", LocalDate.now(), MaritalStatus.MONSIEUR)   ,LocalDate.now(), LocalDate.now(), ReservationStatus.ACTIVE));
        assertFalse(reservationService.checkAvailability("ISBN123", LocalDate.now()));
    }

    @Test
    void testCheckAvailability_ReservationDateOutOfRange() {
        when(bookRepository.findByIsbn(anyString())).thenReturn(new Book("1234567890", "The Lord of the Rings", "J.R.R. Tolkien", "Houghton Mifflin Harcourt", Format.BROCHE, false));
        assertFalse(reservationService.checkAvailability("ISBN123", LocalDate.now().plusMonths(5)));
    }

    // Similarly, write tests for other scenarios in checkAvailability method

    @Test
    void testMakeReservation_Successful() {
        when(bookRepository.findByIsbn(anyString())).thenReturn(new Book("1234567890", "The Lord of the Rings", "J.R.R. Tolkien", "Houghton Mifflin Harcourt", Format.BROCHE, false));
        when(memberRepository.findById(anyString())).thenReturn(new Member(8L, "5", "Qu", "Kev", LocalDate.now(), MaritalStatus.MONSIEUR));

        when(reservationRepository.countByMemberIdAndEndDateIsNull(anyString())).thenReturn(2);
        when(reservationRepository.save(any())).thenReturn(
                new Reservation(4L,
                        new Book("1234567890", "The Lord of the Rings", "J.R.R. Tolkien", "Houghton Mifflin Harcourt", Format.BROCHE, false),
                        new Member(8L, "5", "Qu", "Kev", LocalDate.now(), MaritalStatus.MONSIEUR)   ,LocalDate.now(), LocalDate.now(), ReservationStatus.ACTIVE));

        Reservation reservation = reservationService.makeReservation("ISBN123", LocalDate.now(), "memberId");
        assertNotNull(reservation);
    }

    @Test
    void testMakeReservation_MaxReservationsReached() {
        when(bookRepository.findByIsbn(anyString())).thenReturn(new Book("1234567890", "The Lord of the Rings", "J.R.R. Tolkien", "Houghton Mifflin Harcourt", Format.BROCHE, false));
        when(memberRepository.findById(anyString())).thenReturn(new Member(8L, "5", "Qu", "Kev", LocalDate.now(), MaritalStatus.MONSIEUR));
        when(reservationRepository.countByMemberIdAndEndDateIsNull(anyString())).thenReturn(3);

        assertThrows(IllegalArgumentException.class, () -> reservationService.makeReservation("ISBN123", LocalDate.now(), "memberId"));
    }

    // Similarly, write tests for cancelReservation method
}
