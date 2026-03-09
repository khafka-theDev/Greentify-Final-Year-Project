package org.oss.greentify.Rewards;

import java.io.Serializable;

public class Reward implements Serializable {

    private String id;          // Firebase key or unique identifier
    private String name;
    private String description;
    private int pointsRequired; // Points required to claim (matches Firestore field)
    private String expiryDate;  // Expiry date string (e.g. "2025-12-31")
    private String imageUrl;    // URL to reward image
    private int stock;          // Number of rewards left in stock

    // No-arg constructor required for Firebase deserialization
    public Reward() {
    }

    // Full constructor for manual object creation
    public Reward(String id, String name, String description, int pointsRequired, String expiryDate, String imageUrl, int stock) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.pointsRequired = pointsRequired;
        this.expiryDate = expiryDate;
        this.imageUrl = imageUrl;
        this.stock = stock;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getPointsRequired() {
        return pointsRequired;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getStock() {
        return stock;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPointsRequired(int pointsRequired) {
        this.pointsRequired = pointsRequired;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
}
