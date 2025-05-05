package com.example.mobile_group_assignment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TravelPlan implements Serializable {
    private static final long serialVersionUID = 1L;
    private String destination;
    private String startDate;
    private String endDate;
    private String budgetRange;
    private String travelType;
    private List<String> places;
    private List<DayCard> dayCards = new ArrayList<>(); // Initialize to empty list

    // Primary constructor to handle places as List<String>
    public TravelPlan(String destination, String startDate, String endDate, String budgetRange,
                      String travelType, List<String> places) {
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
        this.budgetRange = budgetRange;
        this.travelType = travelType;
        this.places = places;
    }

    // Convert String to List<String>
    public TravelPlan(String destination, String startDate, String endDate, String budgetRange,
                      String travelType, String placesStr) {
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
        this.budgetRange = budgetRange;
        this.travelType = travelType;

        // Handle places as string (legacy format)
        if (placesStr != null && !placesStr.isEmpty()) {
            try {
                // Try to parse as JSON array
                this.places = new Gson().fromJson(placesStr, new TypeToken<List<String>>(){}.getType());
            } catch (Exception e) {
                // If parsing fails, create singe item list
                this.places = new ArrayList<>();
                this.places.add(placesStr);
            }
        } else {
            this.places = new ArrayList<>();
        }
    }

    // Secondary constructor with day cards
    public TravelPlan(String destination, String startDate, String endDate, String budgetRange,
                      String travelType, List<String> places, List<DayCard> dayCards) {
        this(destination, startDate, endDate, budgetRange, travelType, places);
        if (dayCards != null) {
            this.dayCards = new ArrayList<>(dayCards);
        }
    }

    // JSON serialization/deserialization for day cards
    public String getDayCardsJson() {
        return new Gson().toJson(dayCards);
    }

    public void setDayCardsFromJson(String dayCardsJson) {
        if (dayCardsJson != null && !dayCardsJson.isEmpty()) {
            try {
                this.dayCards = new Gson().fromJson(
                        dayCardsJson, new TypeToken<List<DayCard>>(){}.getType()
                );
            } catch (Exception e) {
                this.dayCards = new ArrayList<>();
            }
        } else {
            this.dayCards = new ArrayList<>();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        TravelPlan other = (TravelPlan) obj;

        // Compare essential properties
        boolean destinationEquals = (destination == null && other.destination == null) ||
                (destination != null && destination.equals(other.destination));
        boolean startDateEquals = (startDate == null && other.startDate == null) ||
                (startDate != null && startDate.equals(other.startDate));
        boolean endDateEquals = (endDate == null && other.endDate == null) ||
                (endDate != null && endDate.equals(other.endDate));
        return destinationEquals && startDateEquals && endDateEquals;
    }

    @Override
    public int hashCode() {
        int result = destination != null ? destination.hashCode() : 0;
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        return result;
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

    // Update places getter to handle both String and List<String>
    public List<String> getPlaces() {
        return places;
    }

    // For backward compatibility, add a method to get places as a String
    public String getPlacesAsString() {
        if (places == null || places.isEmpty()) {
            return "";
        } else if (places.size() == 1) {
            return places.get(0);
        } else {
            return new Gson().toJson(places);
        }
    }

    public void setPlaces(List<String> places) {
        this.places = places != null ? new ArrayList<>(places) : new ArrayList<>();
    }

    // For backward compatibility
    public void setPlaces(String placesStr) {
        if (placesStr != null && !placesStr.isEmpty()) {
            try {
                // Try to parse as JSON array
                this.places = new Gson().fromJson(placesStr, new TypeToken<List<String>>(){}.getType());
            } catch (Exception e) {
                // If parsing fails, create a single-item list
                this.places = new ArrayList<>();
                this.places.add(placesStr);
            }
        } else {
            this.places = new ArrayList<>();
        }
    }

    public List<DayCard> getDayCards() {
        return dayCards;
    }

    public void setDayCards(List<DayCard> dayCards) {
        this.dayCards = dayCards != null ? new ArrayList<>(dayCards) : new ArrayList<>();
    }

    // Helper methods
    public void addDayCard(DayCard dayCard) {
        if (dayCard != null) {
            this.dayCards.add(dayCard);
        }
    }

    public void clearDayCards() {
        this.dayCards.clear();
    }

    @Override
    public String toString() {
        return "TravelPlan{" +
                "destination='" + destination + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", dayCards=" + dayCards.size() +
                '}';
    }
}
