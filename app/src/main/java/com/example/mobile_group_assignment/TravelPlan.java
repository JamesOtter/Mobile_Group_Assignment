package com.example.mobile_group_assignment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.util.List;

public class TravelPlan implements Serializable {
    private static final long serialVersionUID = 1L;  // Add serial version UID for better compatibility during deserialization

    private String destination;
    private String startDate;
    private String endDate;
    private String budgetRange;
    private String travelType;
    private String places;

    private List<DayCard> dayCards; // Add this field

    // Constructor, getters, and setters
    public TravelPlan(String destination, String startDate, String endDate, String budgetRange, String travelType, String places) {
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
        this.budgetRange = budgetRange;
        this.travelType = travelType;
        this.places = places;
    }
    public String getDayCardsJson() {
        Gson gson = new Gson();
        return gson.toJson(dayCards); // Convert the dayCards list to a JSON string
    }
    public void setDayCardsFromJson(String dayCardsJson) {
        Gson gson = new Gson();
        this.dayCards = gson.fromJson(dayCardsJson, new TypeToken<List<DayCard>>() {}.getType());
    }

    // Getters and setters
    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getBudgetRange() {
        return budgetRange;
    }

    public void setBudgetRange(String budgetRange) {
        this.budgetRange = budgetRange;
    }

    public String getTravelType() {
        return travelType;
    }

    public void setTravelType(String travelType) {
        this.travelType = travelType;
    }

    public String getPlaces() {
        return places;
    }

    public void setPlaces(String places) {
        this.places = places;
    }

    public void setDayCards(List<DayCard> dayCards) {
        this.dayCards = dayCards; // 🛠️ now properly sets dayCards
    }
}
