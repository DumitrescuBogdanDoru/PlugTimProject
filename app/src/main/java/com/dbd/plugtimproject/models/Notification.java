package com.dbd.plugtimproject.models;

public class Notification {

    private String userId;
    private String stationId;
    private String text;

    public Notification() {

    }

    public Notification(String userId, String stationId, String text) {
        this.userId = userId;
        this.stationId = stationId;
        this.text = text;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
