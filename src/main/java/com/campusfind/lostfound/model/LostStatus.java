package com.campusfind.lostfound.model;

public enum LostStatus {
    REPORTED("Active / Reported"),
    RESOLVED("Resolved / Returned"),
    CLOSED("Closed / Cancelled");

    private final String label;

    LostStatus(String label) {
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
