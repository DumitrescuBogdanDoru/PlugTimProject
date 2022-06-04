package com.dbd.plugtimproject.models;

public class Visit {

    private String visitId;
    private String stationId;
    private String userId;
    private String comment;
    private boolean positive;
    private boolean negative;

    public Visit(String visitId, String userId, String comment, boolean positive) {
        this.visitId = visitId;
        this.userId = userId;
        this.comment = comment;
        this.positive = positive;
    }

    public String getVisitId() {
        return visitId;
    }

    public void setVisitId(String visitId) {
        this.visitId = visitId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isPositive() {
        return positive;
    }

    public void setPositive(boolean positive) {
        this.positive = positive;
    }
}