package com.dyuksha.model;

/**
 * Lifecycle statuses for an emergency incident.
 */
public enum EmergencyStatus {
    REPORTED("Reported"),
    ASSESSING("Assessing"),
    RESOURCES_ASSIGNED("Resources Assigned"),
    RESPONSE_IN_PROGRESS("Response In Progress"),
    RESOLVED("Resolved");

    private final String displayName;

    EmergencyStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the CSS modifier class used in badge and status-pill styles.
     * Maps directly to CSS classes defined in dashboard.css.
     */
    public String getCssClass() {
        return name().toLowerCase().replace('_', '-');
    }
}

