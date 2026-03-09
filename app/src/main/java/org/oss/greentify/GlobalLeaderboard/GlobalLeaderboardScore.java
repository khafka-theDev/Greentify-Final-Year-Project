package org.oss.greentify.GlobalLeaderboard;

public class GlobalLeaderboardScore {
    private String email;     // New field for email
    private String username;  // Field for username
    private int points;
    private int rank;

    // Required empty constructor for Firebase
    public GlobalLeaderboardScore() {
    }

    // Constructor including email and username
    public GlobalLeaderboardScore(String email, String username, int points) {
        this.email = email;
        this.username = username;
        this.points = points;
    }

    // Getters and Setters for all fields
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}
