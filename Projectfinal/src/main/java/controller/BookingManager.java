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
        Booking cancelledBooking = null; //store's booking we want to cancel
        for (Booking booking : allBookings) { //loop through all bookings
            if (booking.getBookingId().equals(bookingId)) { //if selected booking matches the lists booking
                cancelledBooking = booking;
                break;
            }
        }

        if (cancelledBooking == null) { //if we never found booking with that ID
            return;
        }

        //check if the booking was already confirmed before cancelling it
        boolean wasConfirmed = cancelledBooking.getStatus() == BookingStatus.CONFIRMED;
        cancelledBooking.setStatus(BookingStatus.CANCELLED);

        if (!wasConfirmed) { //if the booking wasn't confirmed yet
            return; //stop the cancelling
        }

        //get the eventID for this booking
        String eventId = cancelledBooking.getEventId();
        Booking firstWaitListed = null; //stores earliest waitlisted booking


        for (Booking booking : allBookings) { //loop through all bookings
            //check for same event ID, as well as if bookings waitlisted
            if (booking.getEventId().equals(eventId) && booking.getStatus() == BookingStatus.WAITLISTED) {
                //if we haven't picked anyone yet or this booking was created earlier than the one we already picked
                if (firstWaitListed == null || booking.getCreatedAt().isBefore(firstWaitListed.getCreatedAt())) {
                    firstWaitListed = booking; //update to earliest waitlisted booking
                }
            }
        }

        if (firstWaitListed != null) { //if we found someone on waitlist
            firstWaitListed.setStatus(BookingStatus.CONFIRMED); //promote them
        }
    }

    public void removeBooking(String bookingId) {
        allBookings.removeIf(b -> b.getBookingId().equals(bookingId));
    }
}