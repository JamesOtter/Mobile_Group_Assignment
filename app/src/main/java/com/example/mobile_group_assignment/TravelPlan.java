package com.example.mobile_group_assignment;

public class TravelPlan {
    private String destination;
    private String date;
    private String notes;

    public TravelPlan(String destination, String date, String notes) {
        this.destination = destination;
        this.date = date;
        this.notes = notes;
    }

    public String getDestination() {
        return destination;
    }

    public String getDate() {
        return date;
    }

    public String getNotes() {
        return notes;
    }
}
