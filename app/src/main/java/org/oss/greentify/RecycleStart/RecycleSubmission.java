package org.oss.greentify.RecycleStart;

public class RecycleSubmission {
    private String userId;
    private String userName; // Added userName for leaderboard display
    private String facilityName;
    private String imageUrl;
    private double weightKg;
    private int points;
    private long timestamp;
    private String materialType;

    // New fields for better leaderboard tracking
    private double totalRecycledWeight;  // Cumulative weight recycled by user
    private int totalPoints;  // Cumulative points earned by the user
    private long lastUpdateTimestamp;  // Last time the user data was updated

    // Required default constructor for Firebase
    public RecycleSubmission() {}

    // Updated constructor with additional fields
    public RecycleSubmission(String userId, String userName, String facilityName, String imageUrl, double weightKg,
                             int points, long timestamp, String materialType, double totalRecycledWeight,
                             int totalPoints, long lastUpdateTimestamp) {
        this.userId = userId;
        this.userName = userName;
        this.facilityName = facilityName;
        this.imageUrl = imageUrl;
        this.weightKg = weightKg;
        this.points = points;
        this.timestamp = timestamp;
        this.materialType = materialType;
        this.totalRecycledWeight = totalRecycledWeight;
        this.totalPoints = totalPoints;
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(double weightKg) {
        this.weightKg = weightKg;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMaterialType() {
        return materialType;
    }

    public void setMaterialType(String materialType) {
        this.materialType = materialType;
    }

    public double getTotalRecycledWeight() {
        return totalRecycledWeight;
    }

    public void setTotalRecycledWeight(double totalRecycledWeight) {
        this.totalRecycledWeight = totalRecycledWeight;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    @Override
    public String toString() {
        return "RecycleSubmission{" +
                "userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", facilityName='" + facilityName + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", weightKg=" + weightKg +
                ", points=" + points +
                ", timestamp=" + timestamp +
                ", materialType='" + materialType + '\'' +
                ", totalRecycledWeight=" + totalRecycledWeight +
                ", totalPoints=" + totalPoints +
                ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                '}';
    }
}
