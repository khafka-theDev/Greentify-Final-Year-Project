package org.oss.greentify.model;

public class User {
    private String uid;
    private String username;
    private String email;
    private String profileImageUrl; // ✅ Use this consistently (Cloudinary URL from Firestore)
    private String role;
    private Long greenCredits;
    private Long points;
    private Object claimedRewards; // Can be List, Map, etc., depending on usage

    // Required for Firestore deserialization
    public User() {}

    public User(String uid, String username, String email) {
        this.uid = uid;
        this.username = username;
        this.email = email;
    }

    // --- Getters and Setters ---

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getGreenCredits() {
        return greenCredits;
    }

    public void setGreenCredits(Long greenCredits) {
        this.greenCredits = greenCredits;
    }

    public Long getPoints() {
        return points;
    }

    public void setPoints(Long points) {
        this.points = points;
    }

    public Object getClaimedRewards() {
        return claimedRewards;
    }

    public void setClaimedRewards(Object claimedRewards) {
        this.claimedRewards = claimedRewards;
    }
}
