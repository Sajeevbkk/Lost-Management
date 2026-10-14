package com.campusfind.lostfound.model;

public enum Category {
    ID_CARD("ID Card & Badges"),
    ELECTRONICS("Electronics & Gadgets"),
    BOOKS("Books & Study Materials"),
    BAGS("Bags & Pouches"),
    WALLETS("Wallets & Purses"),
    KEYS("Keys & Keychains"),
    OTHER("Other Accessories");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
