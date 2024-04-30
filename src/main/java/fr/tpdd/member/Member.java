package fr.tpdd.member;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Member {

    private final Long id;
    private final String membershipCode;
    private final String lastName;
    private final String firstName;
    private final LocalDate dateOfBirth;
    private final MaritalStatus maritalStatus;
    private final List<Reservation> reservations;

    public Member(Long id, String membershipCode, String lastName, String firstName, LocalDate dateOfBirth, MaritalStatus maritalStatus) {
        this.id = id;
        this.membershipCode = membershipCode;
        this.lastName = lastName;
        this.firstName = firstName;
        this.dateOfBirth = dateOfBirth;
        this.maritalStatus = maritalStatus;
        this.reservations = new ArrayList<>();
    }

    // Getter methods for all fields
    public Long getId() {
        return id;
    }

    public String getMembershipCode() {
        return membershipCode;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    // Method to add a reservation
    public void addReservation(Reservation reservation) {
        reservations.add(reservation);
    }

    // Method to cancel a reservation
    public void cancelReservation(Reservation reservation) {
        reservations.remove(reservation);
    }
}
