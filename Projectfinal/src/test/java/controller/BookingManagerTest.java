package controller;

import model.Booking;
import model.BookingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BookingManagerTest {

    private BookingManager bookingManager;

    // This runs before every single test to ensure we start with a clean, empty list
    @BeforeEach
    public void setUp() {
        bookingManager = new BookingManager();
    }

    @Test
    public void testCreateBooking_UnderCapacity_IsConfirmed() {
        // Arrange
        int currentConfirmed = 0;
        int maxCapacity = 5;

        // Act
        bookingManager.createBooking("B1", "U1", "Alice", "Guest", "E1", "Java Workshop", currentConfirmed, maxCapacity);

        // Assert
        Booking savedBooking = bookingManager.getAllBookings().get(0);
        assertEquals(BookingStatus.CONFIRMED, savedBooking.getStatus(), "Booking should be confirmed when under capacity.");
    }

    @Test
    public void testCreateBooking_OverCapacity_GoesToWaitlist() {
        // Arrange: Simulate an event that is already full (5/5 seats taken)
        int currentConfirmed = 5;
        int maxCapacity = 5;

        // Act
        bookingManager.createBooking("B1", "U1", "Bob", "Student", "E1", "Java Workshop", currentConfirmed, maxCapacity);

        // Assert
        Booking savedBooking = bookingManager.getAllBookings().get(0);
        assertEquals(BookingStatus.WAITLISTED, savedBooking.getStatus(), "Booking should be waitlisted when at max capacity.");
    }

    @Test
    public void testIsUserAlreadyBooked_PreventsDuplicates() {
        // Arrange: Create an initial booking
        bookingManager.createBooking("B1", "U1", "Charlie", "Staff", "E1", "Java Workshop", 0, 5);

        // Act & Assert: Check if the system catches the duplicate attempt
        assertTrue(bookingManager.isUserAlreadyBooked("U1", "E1"), "Should return true for an existing active booking.");

        // Check for a different event to ensure it doesn't block everything
        assertFalse(bookingManager.isUserAlreadyBooked("U1", "E2"), "Should return false for a different event.");
    }

    @Test
    public void testUserTypeLimits_AreEnforced() {
        // Arrange: Book 1 ticket for a Guest, 3 for a Student
        bookingManager.createBooking("B1", "GUEST1", "Dan", "Guest", "E1", "Event 1", 0, 5);

        bookingManager.createBooking("B2", "STUDENT1", "Eve", "Student", "E1", "Event 1", 0, 5);
        bookingManager.createBooking("B3", "STUDENT1", "Eve", "Student", "E2", "Event 2", 0, 5);
        bookingManager.createBooking("B4", "STUDENT1", "Eve", "Student", "E3", "Event 3", 0, 5);

        // Act & Assert
        // Guest limit is 1
        assertTrue(bookingManager.hasReachedBookingLimit("GUEST1", "Guest"), "Guest should be blocked after 1 ticket.");
        assertFalse(bookingManager.hasReachedBookingLimit("GUEST2", "Guest"), "New guest should not be blocked.");

        // Student limit is 3
        assertTrue(bookingManager.hasReachedBookingLimit("STUDENT1", "Student"), "Student should be blocked after 3 tickets.");
    }

    @Test
    public void testWaitlistAutoPromotion_OnRemove() {
        // Arrange: Create 1 Confirmed user and 1 Waitlisted user for an event with capacity 1
        bookingManager.createBooking("B1", "U1", "Frank", "Staff", "E1", "Exclusive Seminar", 0, 1);

        // Pause for a tiny fraction of a second to ensure timestamps are slightly different
        try { Thread.sleep(10); } catch (InterruptedException e) { e.printStackTrace(); }

        bookingManager.createBooking("B2", "U2", "Grace", "Staff", "E1", "Exclusive Seminar", 1, 1);

        // Verify initial state
        assertEquals(BookingStatus.CONFIRMED, bookingManager.getAllBookings().get(0).getStatus());
        assertEquals(BookingStatus.WAITLISTED, bookingManager.getAllBookings().get(1).getStatus());

        // Act: Remove the confirmed user
        bookingManager.removeBooking("B1");

        // Assert: Grace should have automatically been bumped to CONFIRMED
        Booking remainingBooking = bookingManager.getAllBookings().get(0);
        assertEquals("U2", remainingBooking.getUserId(), "Grace should be the only one left.");
        assertEquals(BookingStatus.CONFIRMED, remainingBooking.getStatus(), "Grace should have been auto-promoted to confirmed.");
    }
}