package fr.tpdd.member.repository;

import fr.tpdd.book.Book;
import fr.tpdd.member.Member;
import fr.tpdd.member.Reservation;
import fr.tpdd.member.ReservationStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    Reservation findByBookAndReservationDate(Book book, LocalDate reservationDate);

    int countByMemberIdAndEndDateIsNull(String memberId);

    Reservation findById(int reservationId);
}
