package model;

import java.time.LocalDateTime;

public abstract class Workshop extends Event {

    private String topic; //unique feature
    public Workshop(String eventID, String title, LocalDateTime dateTime, String location, int capacity, String topic) {
        super(eventID, title, dateTime, location, capacity);
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }
    @Override
    public  String getEventtype() {
        return "Workshop";
    }
    @Override
    public String extrainfo() {
        return topic;
    }
    @Override
    public void updateExtrainfo(String newInfo) {
        topic = newInfo;
    }
}
