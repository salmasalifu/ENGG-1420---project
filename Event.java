package model;

import java.time.LocalDateTime;

public abstract class Event {
    private String eventID;
    private String title;
    private LocalDateTime dateTime;
    private String location;
    private int capacity;
    private EventStatus status;

    public Event(String eventID, String title, LocalDateTime dateTime, String location, int capacity) {

        //new event
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0.");
        }
        this.eventID = eventID;
        this.title = title;
        this.dateTime = dateTime;
        this.location = location;
        this.capacity = capacity;
        this.status = EventStatus.Active;
    }

    //create event
    public String getID() { return eventID;}
    public String getTitle() { return title; }
    public LocalDateTime getDateTime() { return dateTime; }
    public String getLocation() { return location; }
    public int getCapacity() { return capacity; }
    public EventStatus getStatus() { return status; }

    //update info
    public void cancelEvent() {
        this.status = EventStatus.Cancelled;
    }
    public void updateCapacity(int newCapacity) {
        if (newCapacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }
        this.capacity = newCapacity;
    }
    //event type
    public abstract String getEventtype(); //event type
    public abstract String extrainfo();
    public abstract void updateExtrainfo(String newInfo);


}
