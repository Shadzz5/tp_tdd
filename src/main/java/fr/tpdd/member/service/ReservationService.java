package fr.tpdd.member.service;

import fr.tpdd.book.Book;
import fr.tpdd.book.repository.BookRepository;
import fr.tpdd.book.service.BookService;
import fr.tpdd.member.MaritalStatus;
import fr.tpdd.member.Member;
import fr.tpdd.member.Reservation;
import fr.tpdd.member.ReservationStatus;
import fr.tpdd.member.repository.MemberRepository;
import fr.tpdd.member.repository.ReservationRepository;

import java.time.LocalDate;
import java.util.List;

public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final BookService bookService;
    private final BookRepository bookRepository;
    public ReservationService(ReservationRepository reservationRepository, MemberRepository memberRepository, BookService bookService, BookRepository bookRepository) {
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.bookService = bookService;
        this.bookRepository = bookRepository;
    }
    public boolean checkAvailability(String isbn, LocalDate reservationDate) {
        Book book = bookRepository.findByIsbn(isbn);
        if (book == null) {
            return false;
        }

        Reservation existingReservation = reservationRepository.findByBookAndReservationDate(book, reservationDate);
        if (existingReservation != null) {
            return false;
        }

        LocalDate today = LocalDate.now();
        LocalDate maxReservationDate = today.plusMonths(4);
        if (reservationDate.isBefore(today) || reservationDate.isAfter(maxReservationDate)) {
            return false;
        }
        return true;
    }

    public Reservation makeReservation(String isbn, LocalDate reservationDate, String memberId) {
        if (!checkAvailability(isbn, reservationDate)) {
            return null;
        }

        int openReservationsCount = reservationRepository.countByMemberIdAndEndDateIsNull(memberId);
        if (openReservationsCount >= 3) {
            throw new IllegalArgumentException("Member has reached the maximum number of open reservations");
        }
        Member member = memberRepository.findById(memberId);
        // Create a new reservation and save it
        Book book = bookRepository.findByIsbn(isbn);
        Reservation reservation = new Reservation(1L, book,member, reservationDate, LocalDate.now(), ReservationStatus.ACTIVE);
        reservationRepository.save(reservation);

        return reservation;
    }
    public boolean cancelReservation(int reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId);
        if (reservation == null) {
            return false;
        }

        if (reservation.getDueDate() != null) {
            return false;
        }

        reservation.setDueDate(LocalDate.now());
        reservationRepository.save(reservation);

        return true;
    }

}


