package com.dyuksha.model;

/**
 * Availability status of a field resource or facility.
 */
public enum ResourceStatus {
    AVAILABLE("Available"),
    ASSIGNED("Assigned"),
    BUSY("Busy"),
    OFFLINE("Offline");

    private final String displayName;

    ResourceStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCssClass() {
        return name().toLowerCase();
    }
}

