package model;

import java.time.LocalDateTime;

public class Booking {
    private String bookingId;
    private String userId;
    private String userName;   // We keep the name here so the UI doesn't have to do heavy lookups
    private String eventId;
    private String eventTitle; // We keep the title here for the same reason
    private BookingStatus status;
    private LocalDateTime createdAt; // Used to figure out who has been on the waitlist the longest!

    public Booking(String bookingId, String userId, String userName, String eventId, String eventTitle, BookingStatus status) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.userName = userName;
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.status = status;
        this.createdAt = LocalDateTime.now(); // Defaults to right now
    }

    // --- Standard Getters that JavaFX needs for the tables ---
    public String getBookingId() { return bookingId; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getEventId() { return eventId; }
    public String getEventTitle() { return eventTitle; }
    public BookingStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // --- Setters so the DataManager and BookingManager can update statuses ---
    public void setStatus(BookingStatus status) { this.status = status; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }
}