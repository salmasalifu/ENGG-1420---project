package model;

import java.time.LocalDateTime;

public class Seminar extends Event {
    private String speaker;

    public Seminar(String eventID, String title, LocalDateTime dateTime, String location, int capacity, String speaker) {
        super(eventID, title, dateTime, location, capacity);
        this.speaker = speaker;
    }

    @Override
    public String getEventtype() { return "Seminar"; }

    @Override
    public String getExtrainfo() { return speaker; } // Matches Event.java

    @Override
    public void updateExtrainfo(String newInfo) { this.speaker = newInfo; }
}