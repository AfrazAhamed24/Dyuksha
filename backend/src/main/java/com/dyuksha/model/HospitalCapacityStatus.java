package com.dyuksha.model;

/**
 * Operating capacity level for hospitals.
 */
public enum HospitalCapacityStatus {
    NORMAL("Normal Capacity"),
    SURGE("Surge Operations"),
    CRITICAL("Near Maximum Capacity"),
    DIVERT("Diversion / Full");

    private final String displayName;

    HospitalCapacityStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCssClass() {
        return name().toLowerCase();
    }
}

