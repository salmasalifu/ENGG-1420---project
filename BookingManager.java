package controller;

import model.Booking;
import model.BookingStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

public class BookingManager {
    private List<Booking> allBookings = new ArrayList<>();

    /**
     * Enforces maximum active (Confirmed) booking limits by user type
     */
    private int getMaxBookings(String userType) {
        switch (userType.toLowerCase()) {
            case "student": return 3; // [cite: 64]
            case "staff": return 5;   // [cite: 65]
            case "guest": return 1;   // [cite: 66]
            default: return 0;
        }
    }

    /**
     * Core logic to create a booking while enforcing all system rules[
     */
    public String createBooking(String bookingId, String userId, String userType,
                                String eventId, int currentConfirmedCount, int maxCapacity) {

        // Rule: A user cannot book the same event twice
        for (Booking b : allBookings) {
            if (b.getUserId().equals(userId) && b.getEventId().equals(eventId) &&
                    (b.getStatus() != BookingStatus.CANCELLED)) {
                return "Error: User is already registered for this event.";
            }
        }

        // Rule: Enforce user-type confirmed booking limits
        long activeConfirmedCount = allBookings.stream()
                .filter(b -> b.getUserId().equals(userId) && b.getStatus() == BookingStatus.CONFIRMED)
                .count();

        if (activeConfirmedCount >= getMaxBookings(userType)) {
            return "Error: Maximum confirmed booking limit reached for " + userType;
        }

        // Rule: Determine status based on event capacity
        BookingStatus initialStatus;
        if (currentConfirmedCount < maxCapacity) {
            initialStatus = BookingStatus.CONFIRMED;
        } else {
            initialStatus = BookingStatus.WAITLISTED; //
        }

        Booking newBooking = new Booking(bookingId, userId, eventId, initialStatus);
        allBookings.add(newBooking);

        return "Booking " + bookingId + " created as: " + initialStatus;
    }

    /**
     * Cancels a booking and triggers automatic promotion if a confirmed seat opens
     */
    public void cancelBooking(String bookingId) {
        Booking bookingToCancel = allBookings.stream()
                .filter(b -> b.getBookingId().equals(bookingId))
                .findFirst()
                .orElse(null);

        if (bookingToCancel != null && bookingToCancel.getStatus() != BookingStatus.CANCELLED) {
            BookingStatus oldStatus = bookingToCancel.getStatus();
            bookingToCancel.setStatus(BookingStatus.CANCELLED);

            // If a confirmed spot was freed, promote the first waitlisted user
            if (oldStatus == BookingStatus.CONFIRMED) {
                promoteFromWaitlist(bookingToCancel.getEventId());
            }
        }
    }

    private void promoteFromWaitlist(String eventId) {
        // Rule: Promote based on createdAt (earliest first)
        Booking nextInLine = allBookings.stream()
                .filter(b -> b.getEventId().equals(eventId) && b.getStatus() == BookingStatus.WAITLISTED)
                .min(Comparator.comparing(Booking::getCreatedAt))
                .orElse(null);

        if (nextInLine != null) {
            nextInLine.setStatus(BookingStatus.CONFIRMED);
        }
    }
}