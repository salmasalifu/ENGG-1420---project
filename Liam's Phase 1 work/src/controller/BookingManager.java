
package controller;

import model.Booking;
import model.BookingStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

public class BookingManager {
    private List<Booking> allBookings = new ArrayList<>();

    private int getMaxBookings(String userType) {
        switch (userType.toLowerCase()) {
            case "student": return 3;
            case "staff": return 5;
            case "guest": return 1;
            default: return 0;
        }
    }

    public String createBooking(String bookingId, String userId, String userType,
                                String eventId, int currentConfirmedCount, int maxCapacity) {

        for (Booking b : allBookings) {
            if (b.getUserId().equals(userId) && b.getEventId().equals(eventId) &&
                    b.getStatus() != BookingStatus.CANCELLED) {
                return "Error: User is already registered for this event.";
            }
        }

        long activeConfirmedCount = allBookings.stream()
                .filter(b -> b.getUserId().equals(userId) && b.getStatus() == BookingStatus.CONFIRMED)
                .count();

        if (activeConfirmedCount >= getMaxBookings(userType)) {
            return "Error: Maximum confirmed booking limit reached for " + userType;
        }

        BookingStatus initialStatus;
        if (currentConfirmedCount < maxCapacity) {
            initialStatus = BookingStatus.CONFIRMED;
        } else {
            initialStatus = BookingStatus.WAITLISTED;
        }

        Booking newBooking = new Booking(bookingId, userId, eventId, initialStatus);
        allBookings.add(newBooking);

        return "Booking " + bookingId + " created as: " + initialStatus;
    }

    public void cancelBooking(String bookingId) {
        Booking bookingToCancel = allBookings.stream()
                .filter(b -> b.getBookingId().equals(bookingId))
                .findFirst()
                .orElse(null);

        if (bookingToCancel != null && bookingToCancel.getStatus() != BookingStatus.CANCELLED) {
            BookingStatus oldStatus = bookingToCancel.getStatus();
            bookingToCancel.setStatus(BookingStatus.CANCELLED);

            if (oldStatus == BookingStatus.CONFIRMED) {
                promoteFromWaitlist(bookingToCancel.getEventId());
            }
        }
    }

    private void promoteFromWaitlist(String eventId) {
        Booking nextInLine = allBookings.stream()
                .filter(b -> b.getEventId().equals(eventId) && b.getStatus() == BookingStatus.WAITLISTED)
                .min(Comparator.comparing(Booking::getCreatedAt))
                .orElse(null);

        if (nextInLine != null) {
            nextInLine.setStatus(BookingStatus.CONFIRMED);
            System.out.println("Waitlist Promotion: User " + nextInLine.getUserId() + " is now CONFIRMED for event " + eventId);
        }
    }
}