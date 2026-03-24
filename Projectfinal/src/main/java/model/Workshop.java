package model;

import java.time.LocalDateTime;

public class Workshop extends Event {
    private String topic;

    public Workshop(String eventID, String title, LocalDateTime dateTime, String location, int capacity, String topic) {
        super(eventID, title, dateTime, location, capacity);
        this.topic = topic;
    }

    @Override
    public String getEventtype() { return "Workshop"; }

    @Override
    public String getExtrainfo() { return topic; } // Matches Event.java

    @Override
    public void updateExtrainfo(String newInfo) { this.topic = newInfo; }
}