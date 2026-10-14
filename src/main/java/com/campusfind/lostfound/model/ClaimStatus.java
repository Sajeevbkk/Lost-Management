package com.campusfind.lostfound.model;

public enum ClaimStatus {
    PENDING("Pending Verification"),
    UNDER_VERIFICATION("Under Verification"),
    APPROVED("Approved for Collection"),
    REJECTED("Claim Rejected"),
    RETURNED("Completed / Handed Over");

    private final String label;

    ClaimStatus(String label) {
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
