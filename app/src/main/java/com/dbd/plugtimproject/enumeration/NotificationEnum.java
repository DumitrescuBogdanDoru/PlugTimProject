package com.dbd.plugtimproject.enumeration;

public enum NotificationEnum {
    LIKE("like"),
    PHOTO("photo"),
    VISIT("visit");

    private String type;

    NotificationEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
