package org.oss.greentify.RecycleStart;

public class RecycleData {
    private String materialType;
    private double weight;
    private int points;
    private String imageUrl;
    private String location;

    // No-arg constructor required for Firestore
    public RecycleData() {
    }

    public RecycleData(String materialType, double weight, int points, String imageUrl, String location) {
        this.materialType = materialType;
        this.weight = weight;
        this.points = points;
        this.imageUrl = imageUrl;
        this.location = location;
    }

    public String getMaterialType() {
        return materialType;
    }

    public void setMaterialType(String materialType) {
        this.materialType = materialType;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
