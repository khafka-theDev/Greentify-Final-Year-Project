package org.oss.greentify.GlobalLeaderboard;

public class LeaderboardScore implements Comparable<LeaderboardScore> {

    private String username;
    private int score;

    // No-argument constructor required for Firestore
    public LeaderboardScore() {
    }

    public LeaderboardScore(String username, int score) {
        this.username = username;
        this.score = score;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public int compareTo(LeaderboardScore other) {
        // Descending order based on score
        return Integer.compare(other.score, this.score);
    }
}
