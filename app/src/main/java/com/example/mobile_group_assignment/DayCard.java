package com.example.mobile_group_assignment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DayCard implements Serializable {
    private String title;
    private List<String> selectedPlaces;
    private List<String> selectedPlaceImageUrls;

    public DayCard(String title) {
        this.title = title;
        this.selectedPlaces = new ArrayList<>();
        this.selectedPlaceImageUrls = new ArrayList<>();
    }

    public List<String> getSelectedPlaces() {
        return selectedPlaces;
    }

    public void setSelectedPlaces(List<String> selectedPlaces) {
        this.selectedPlaces = selectedPlaces;
    }

    public List<String> getSelectedPlaceImageUrls() {
        return selectedPlaceImageUrls;
    }

    public void setSelectedPlaceImageUrls(List<String> selectedPlaceImageUrls) {
        this.selectedPlaceImageUrls = selectedPlaceImageUrls;
    }

    public String getTitle() {
        return title;
    }
}
