package controller;

import model.Booking;
import model.BookingStatus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BookingManager {
    private List<Booking> allBookings = new ArrayList<>();

    public List<Booking> getAllBookings() {
        return allBookings;
    }

    // Checks if a user is already actively booked for a specific event
    public boolean isUserAlreadyBooked(String userId, String eventId) {
        return allBookings.stream()
                .anyMatch(b -> b.getUserId().equals(userId) &&
                        b.getEventId().equals(eventId) &&
                        b.getStatus() != BookingStatus.CANCELLED);
    }

    // Creates the booking and decides if they get a seat or hit the waitlist
    public String createBooking(String bookingId, String userId, String userName, String userType, String eventId, String eventTitle, int currentConfirmedCount, int maxCapacity) {
        BookingStatus initialStatus = (currentConfirmedCount < maxCapacity) ? BookingStatus.CONFIRMED : BookingStatus.WAITLISTED;

        Booking newBooking = new Booking(bookingId, userId, userName, eventId, eventTitle, initialStatus);
        allBookings.add(newBooking);

        return "Booking " + bookingId + " created as: " + initialStatus;
    }

    // Cancels a booking and automatically triggers the waitlist if a seat opens up
    public String cancelBooking(String bookingId) {
        Booking target = allBookings.stream().filter(b -> b.getBookingId().equals(bookingId)).findFirst().orElse(null);

        if (target != null) {
            boolean wasConfirmed = target.getStatus() == BookingStatus.CONFIRMED;
            target.setStatus(BookingStatus.CANCELLED);

            // If they had a confirmed seat, give it to the next person!
            if (wasConfirmed) {
                return promoteNextOnWaitlist(target.getEventId());
            }
        }
        return "Booking cancelled successfully.";
    }

    // Completely deletes a booking from the system (like when an admin kicks someone out)
    public String removeBooking(String bookingId) {
        Booking target = allBookings.stream().filter(b -> b.getBookingId().equals(bookingId)).findFirst().orElse(null);

        if (target != null) {
            boolean wasConfirmed = target.getStatus() == BookingStatus.CONFIRMED;
            allBookings.remove(target);

            if (wasConfirmed) {
                return promoteNextOnWaitlist(target.getEventId());
            }
        }
        return "Booking removed successfully.";
    }

    // The Auto-Promoter: Finds the oldest waitlisted person and bumps them up
    public String promoteNextOnWaitlist(String eventId) {
        Booking promotedBooking = allBookings.stream()
                .filter(b -> b.getEventId().equals(eventId) && b.getStatus() == BookingStatus.WAITLISTED)
                .min(Comparator.comparing(Booking::getCreatedAt)) // Finds the earliest timestamp
                .orElse(null);

        if (promotedBooking != null) {
            promotedBooking.setStatus(BookingStatus.CONFIRMED);
            return "🔔 Notification: " + promotedBooking.getUserName() + " was auto-promoted to CONFIRMED!";
        }
        return "Seat freed. No one was on the waitlist to promote.";
    }
}