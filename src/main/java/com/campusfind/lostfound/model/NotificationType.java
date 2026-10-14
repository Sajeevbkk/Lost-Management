package com.campusfind.lostfound.model;

public enum NotificationType {
    POTENTIAL_MATCH("Potential Match Alert"),
    CLAIM_UPDATE("Claim Status Update"),
    SYSTEM_ALERT("Campus Announcement");

    private final String label;

    NotificationType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
