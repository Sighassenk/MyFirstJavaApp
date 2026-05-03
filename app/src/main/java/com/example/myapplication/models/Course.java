package com.example.myapplication.models;

import java.util.HashMap;
import java.util.Map;

public class Course {
    private String id;
    private String title;
    private String description;
    private double price;
    private String instructorId;
    private String instructorName;
    private String category;
    private float rating;
    private int ratingCount;
    private double totalRatingSum;
    private String thumbnailUrl;
    private int totalDuration; // Total seconds of all lessons
    private long createdAt;
    private Map<String, Float> userRatings = new HashMap<>(); // userId -> rating

    public Course() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getInstructorId() { return instructorId; }
    public void setInstructorId(String instructorId) { this.instructorId = instructorId; }

    public String getInstructorName() { return instructorName; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public int getRatingCount() { return ratingCount; }
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }

    public double getTotalRatingSum() { return totalRatingSum; }
    public void setTotalRatingSum(double totalRatingSum) { this.totalRatingSum = totalRatingSum; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public int getTotalDuration() { return totalDuration; }
    public void setTotalDuration(int totalDuration) { this.totalDuration = totalDuration; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public Map<String, Float> getUserRatings() { return userRatings; }
    public void setUserRatings(Map<String, Float> userRatings) { this.userRatings = userRatings; }
    
    public void updateRating() {
        if (userRatings == null || userRatings.isEmpty()) {
            this.rating = 0;
            this.ratingCount = 0;
            return;
        }
        double sum = 0;
        for (float r : userRatings.values()) {
            sum += r;
        }
        this.ratingCount = userRatings.size();
        this.rating = (float) (sum / ratingCount);
    }
}
