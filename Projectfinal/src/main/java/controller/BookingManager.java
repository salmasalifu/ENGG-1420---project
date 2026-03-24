package controller;

import model.Booking;
import model.BookingStatus;

import java.util.ArrayList;
import java.util.List;

public class BookingManager {
    private List<Booking> allBookings = new ArrayList<>();

    public List<Booking> getAllBookings() {
        return allBookings;
    }

    public String createBooking(String bookingId, String userId, String userName, String userType, String eventId, String eventTitle, int currentConfirmedCount, int maxCapacity) {
        BookingStatus initialStatus;
        if (currentConfirmedCount < maxCapacity) {
            initialStatus = BookingStatus.CONFIRMED;
        } else {
            initialStatus = BookingStatus.WAITLISTED;
        }

        Booking newBooking = new Booking(bookingId, userId, userName, eventId, eventTitle, initialStatus);
        allBookings.add(newBooking);

        return "Booking " + bookingId + " created as: " + initialStatus;
    }

    public void cancelBooking(String bookingId) {
        allBookings.stream()
                .filter(b -> b.getBookingId().equals(bookingId))
                .findFirst()
                .ifPresent(b -> b.setStatus(BookingStatus.CANCELLED));
    }

    public void removeBooking(String bookingId) {
        allBookings.removeIf(b -> b.getBookingId().equals(bookingId));
    }
}