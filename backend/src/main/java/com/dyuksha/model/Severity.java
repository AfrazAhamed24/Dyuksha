package com.dyuksha.model;

/**
 * Severity levels for emergency incidents.
 */
public enum Severity {
    CRITICAL("Critical"),
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low");

    private final String displayName;

    Severity(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Returns lowercase CSS class name used in dashboard.css badge styles. */
    public String getCssClass() {
        return name().toLowerCase();
    }
}

