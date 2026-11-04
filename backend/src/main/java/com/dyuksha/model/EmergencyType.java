package com.dyuksha.model;

/**
 * Emergency type classifications for incident reporting.
 */
public enum EmergencyType {
    FLOOD("Flood"),
    FIRE("Fire"),
    MEDICAL_EMERGENCY("Medical Emergency"),
    BUILDING_COLLAPSE("Building Collapse"),
    ROAD_ACCIDENT("Road Accident"),
    OTHER("Other");

    private final String displayName;

    EmergencyType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

