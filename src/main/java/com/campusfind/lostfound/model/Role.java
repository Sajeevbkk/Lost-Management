package com.campusfind.lostfound.model;

public enum Role {
    ADMIN("Campus Administrator"),
    STUDENT("Student"),
    STAFF("Faculty / Staff");

    private final String label;

    Role(String label) {
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
