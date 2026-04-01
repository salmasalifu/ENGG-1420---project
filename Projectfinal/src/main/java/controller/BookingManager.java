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

    // Checks if the user is already signed up for this specific event to prevent double-booking.
    public boolean isUserAlreadyBooked(String userId, String eventId) {
        return allBookings.stream()
                .anyMatch(b -> b.getUserId().equals(userId) &&
                        b.getEventId().equals(eventId) &&
                        b.getStatus() != BookingStatus.CANCELLED);
    }

    // Counts active bookings to make sure Guests, Students, and Staff stay within their limits.
    public boolean hasReachedBookingLimit(String userId, String userType) {
        long activeCount = allBookings.stream()
                .filter(b -> b.getUserId().equals(userId) && b.getStatus() != BookingStatus.CANCELLED)
                .count();

        if (userType.equalsIgnoreCase("Guest") && activeCount >= 1) return true;
        if (userType.equalsIgnoreCase("Student") && activeCount >= 3) return true;
        if (userType.equalsIgnoreCase("Staff") && activeCount >= 5) return true;

        return false;
    }

    // Handles the creation of a new booking and decides if they get a spot or go to the waitlist.
    public String createBooking(String bookingId, String userId, String userName, String userType, String eventId, String eventTitle, int currentConfirmedCount, int maxCapacity) {
        BookingStatus initialStatus = (currentConfirmedCount < maxCapacity) ? BookingStatus.CONFIRMED : BookingStatus.WAITLISTED;

        Booking newBooking = new Booking(bookingId, userId, userName, eventId, eventTitle, initialStatus);
        allBookings.add(newBooking);

        return "Booking " + bookingId + " created as " + initialStatus + ".";
    }

    // Removes a user from the system and mentions their name in the confirmation message.
    public String removeBooking(String bookingId) {
        Booking target = allBookings.stream()
                .filter(b -> b.getBookingId().equals(bookingId))
                .findFirst()
                .orElse(null);

        if (target != null) {
            String name = target.getUserName();
            boolean wasConfirmed = target.getStatus() == BookingStatus.CONFIRMED;
            allBookings.remove(target);

            // If the person leaving had a confirmed seat, we automatically fill it from the waitlist.
            if (wasConfirmed) {
                String promotionResult = promoteNextOnWaitlist(target.getEventId());
                return "User [" + name + "] has been removed. " + promotionResult;
            }
            return "User [" + name + "] has been removed from the waitlist.";
        }
        return "The system could not find that booking ID.";
    }

    // Looks for the next person in line based on when they signed up and gives them the open seat.
    public String promoteNextOnWaitlist(String eventId) {
        Booking promotedBooking = allBookings.stream()
                .filter(b -> b.getEventId().equals(eventId) && b.getStatus() == BookingStatus.WAITLISTED)
                .min(Comparator.comparing(Booking::getCreatedAt))
                .orElse(null);

        if (promotedBooking != null) {
            promotedBooking.setStatus(BookingStatus.CONFIRMED);
            return "Notification: " + promotedBooking.getUserName() + " has been moved from the waitlist to CONFIRMED.";
        }
        return "The seat is now available as there is no one on the waitlist.";
    }

    // Updates a booking status to cancelled without removing the record from history.
    public String cancelBooking(String bookingId) {
        Booking target = allBookings.stream().filter(b -> b.getBookingId().equals(bookingId)).findFirst().orElse(null);

        if (target != null) {
            boolean wasConfirmed = target.getStatus() == BookingStatus.CONFIRMED;
            target.setStatus(BookingStatus.CANCELLED);

            if (wasConfirmed) {
                return promoteNextOnWaitlist(target.getEventId());
            }
        }
        return "The booking has been successfully cancelled.";
    }
}