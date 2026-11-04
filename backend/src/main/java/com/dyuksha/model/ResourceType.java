package com.dyuksha.model;

/**
 * Types of emergency resources across responding agencies.
 */
public enum ResourceType {
    AMBULANCE("Ambulance"),
    FIRE_AND_RESCUE("Fire and Rescue Team"),
    POLICE_UNIT("Police Unit"),
    HOSPITAL("Hospital");

    private final String displayName;

    ResourceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCategoryKey() {
        return switch (this) {
            case AMBULANCE -> "ambulances";
            case FIRE_AND_RESCUE -> "fire";
            case POLICE_UNIT -> "police";
            case HOSPITAL -> "hospitals";
        };
    }
}

