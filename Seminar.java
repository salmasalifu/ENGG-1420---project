package model;

import java.time.LocalDateTime;


public class Seminar extends Event {
    private String speakerName; //unique feature

    public Seminar(String eventID, String title, LocalDateTime dateTime, String location, int capacity, String speakerName) {
        super(eventID, title, dateTime, location, capacity);
        this.speakerName = speakerName;
    }

    public String getspeakerName() {
        return speakerName;
    }

    @Override
    public String getEventtype() {
        return "Seminar";
    }

    @Override
    public String extrainfo() {
        return speakerName;
    }
    @Override
    public void updateExtrainfo(String newInfo) {
        speakerName = newInfo;
    }
}



