package com.toolbarter.enums;

public enum TrustLevel {
    NEW_NEIGHBOUR("New Neighbour"),
    REGULAR_BORROWER("Regular Borrower"),
    RELIABLE_LENDER("Reliable Lender"),
    TRUSTED_NEIGHBOUR("Trusted Neighbour"),
    STREET_CHAMPION("Street Champion");

    private final String displayName;

    TrustLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
