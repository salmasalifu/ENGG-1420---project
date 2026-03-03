package model;

import java.time.LocalDateTime;

public class Concert extends Event {
    private int ageRe;

    public Concert(String eventID, String title, LocalDateTime dateTime, String location, int capacity, int ageRe) {
        super(eventID, title, dateTime, location, capacity);
        if(ageRe<0||ageRe>100){
            throw new IllegalArgumentException("Age must be between 0-100");
        }
        this.ageRe = ageRe;
    }
    public int getageRestriction() {
        return ageRe;
    }
    @Override
    public String getEventtype() {
        return "Concert";
    }
    @Override
    public String extrainfo() {
        return Integer.toString(ageRe);
    }
    @Override
    public void updateExtrainfo(String newInfo) {
        ageRe=Integer.parseInt(newInfo);
    }

    }
