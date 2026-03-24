package model;

import java.time.LocalDateTime;

public class Concert extends Event {
    private String ageLimit; // Changed from int to String

    public Concert(String eventID, String title, LocalDateTime dateTime, String location, int capacity, String ageLimit) {
        super(eventID, title, dateTime, location, capacity);
        this.ageLimit = ageLimit;
    }

    @Override
    public String getEventtype() { return "Concert"; }

    @Override
    public String getExtrainfo() { return ageLimit; } // Matches Event.java

    @Override
    public void updateExtrainfo(String newInfo) { this.ageLimit = newInfo; }
}