package com.toolbarter.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ItemCondition {
    EXCELLENT("Excellent - Like New"),
    GOOD("Good - Fully Functional"),
    FAIR("Fair - Minor Wear"),
    WELL_USED("Well Used - Heavy Signs of Use");

    private final String displayName;

    ItemCondition(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static ItemCondition fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return GOOD;
        }
        String clean = value.trim().toUpperCase().replace(" ", "_").replace("-", "_");
        for (ItemCondition cond : values()) {
            if (cond.name().equalsIgnoreCase(clean) || cond.displayName.equalsIgnoreCase(value.trim())) {
                return cond;
            }
        }
        return GOOD;
    }
}
