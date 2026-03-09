package org.oss.greentify.Home;

import java.util.Date;

public class HistoryItem {
    private String materialType;
    private String weight;
    private String location;
    private String dateSubmitted;
    private String pointGained;
    private String greenCreditGained;
    private String status;
    private String note;
    private boolean isRead; // ✅ NEW: unread indicator
    private Date actualTimestamp; // ✅ NEW: for sorting by time

    // ✅ Full constructor
    public HistoryItem(String materialType, String weight, String location, String dateSubmitted, String pointGained, String greenCreditGained, String status, String note, boolean isRead, Date actualTimestamp) {
        this.materialType = materialType;
        this.weight = weight;
        this.location = location;
        this.dateSubmitted = dateSubmitted;
        this.pointGained = pointGained;
        this.greenCreditGained = greenCreditGained;
        this.status = status;
        this.note = note;
        this.isRead = isRead;
        this.actualTimestamp = actualTimestamp;
    }

    // ======= Getters and Setters =======

    public String getMaterialType() {
        return materialType;
    }

    public void setMaterialType(String materialType) {
        this.materialType = materialType;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDateSubmitted() {
        return dateSubmitted;
    }

    public void setDateSubmitted(String dateSubmitted) {
        this.dateSubmitted = dateSubmitted;
    }

    public String getPointGained() {
        return pointGained;
    }

    public void setPointGained(String pointGained) {
        this.pointGained = pointGained;
    }

    public String getGreenCreditGained() {
        return greenCreditGained;
    }

    public void setGreenCreditGained(String greenCreditGained) {
        this.greenCreditGained = greenCreditGained;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public Date getActualTimestamp() {
        return actualTimestamp;
    }

    public void setActualTimestamp(Date actualTimestamp) {
        this.actualTimestamp = actualTimestamp;
    }
}
