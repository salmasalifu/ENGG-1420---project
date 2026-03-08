package model;

import java.time.LocalDateTime;

public class Booking {
    private String bookingId;   // Unique ID
    private String userId;      // ID of the User
    private String eventId;     // ID of the Event
    private LocalDateTime createdAt; // Timestamp for waitlist order =
    private BookingStatus status;    // Confirmed, Waitlisted, or Cancelled =

    public Booking(String bookingId, String userId, String eventId, BookingStatus status) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.eventId = eventId;
        this.createdAt = LocalDateTime.now();
        this.status = status;
    }

    // Getters and Setters
    public String getBookingId() { return bookingId; }
    public String getUserId() { return userId; }
    public String getEventId() { return eventId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public BookingStatus getStatus() { return status; }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }
}