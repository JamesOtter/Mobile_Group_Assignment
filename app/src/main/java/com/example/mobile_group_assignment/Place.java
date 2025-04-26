package com.example.mobile_group_assignment;

import com.google.firebase.firestore.Exclude;

public class Place {
    private String documentId;
    private String name;
    private String photoUrl;
    private String type;
    private String visitationTime;
    private String location;
    private int duration; // in minutes
    private double cost;
    private String description;
    private String createdBy; // User ID of the creator

    // Empty constructor needed for Firestore
    public Place() {}

    @Exclude
    public String getDocumentId() { return documentId; }

    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public Place(String name, String photoUrl, String type, String visitationTime,
                 String location, int duration, double cost, String description, String createdBy) {
        this.name = name;
        this.photoUrl = photoUrl;
        this.type = type;
        this.visitationTime = visitationTime;
        this.location = location;
        this.duration = duration;
        this.cost = cost;
        this.description = description;
        this.createdBy = createdBy;
    }

    // Getters and setters for all fields
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getVisitationTime() { return visitationTime; }
    public void setVisitationTime(String visitationTime) { this.visitationTime = visitationTime; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
