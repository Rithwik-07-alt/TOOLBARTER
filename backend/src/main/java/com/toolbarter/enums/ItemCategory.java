package com.toolbarter.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ItemCategory {
    POWER_TOOLS("Power Tools"),
    LADDERS_ACCESS("Ladders & Access"),
    GARDEN_YARD("Garden & Yard"),
    CLEANING_EQUIPMENT("Cleaning Equipment"),
    PAINTING_DECORATING("Painting & Decorating"),
    EVENT_PARTY_GEAR("Event & Party Gear"),
    OTHER("Other");

    private final String displayName;

    ItemCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static ItemCategory fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return OTHER;
        }
        String clean = value.trim().toUpperCase().replace(" ", "_").replace("&", "").replace("__", "_");
        for (ItemCategory cat : values()) {
            if (cat.name().equalsIgnoreCase(clean) || cat.displayName.equalsIgnoreCase(value.trim())) {
                return cat;
            }
        }
        return OTHER;
    }
}
