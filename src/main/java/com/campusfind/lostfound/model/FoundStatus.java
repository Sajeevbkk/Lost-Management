package com.campusfind.lostfound.model;

public enum FoundStatus {
    AVAILABLE("Available in Storage"),
    CLAIM_PENDING("Claim Under Review"),
    RETURNED("Returned to Owner");

    private final String label;

    FoundStatus(String label) {
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
