package com.dyuksha.dto;

import com.dyuksha.model.EmergencyType;
import com.dyuksha.model.Severity;
import jakarta.validation.constraints.*;

/**
 * Form-binding DTO for creating a new emergency incident.
 * Validated via Spring MVC {@code @Valid} in the controller.
 */
public class EmergencyForm {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    @NotNull(message = "Emergency type is required")
    private EmergencyType type;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotBlank(message = "Location is required")
    @Size(max = 300, message = "Location must not exceed 300 characters")
    private String locationName;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", inclusive = true, message = "Latitude must be between −90 and 90")
    @DecimalMax(value = "90.0",  inclusive = true, message = "Latitude must be between −90 and 90")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", inclusive = true, message = "Longitude must be between −180 and 180")
    @DecimalMax(value = "180.0",  inclusive = true, message = "Longitude must be between −180 and 180")
    private Double longitude;

    @NotNull(message = "Severity is required")
    private Severity severity;

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public EmergencyType getType() { return type; }
    public void setType(EmergencyType type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
}

