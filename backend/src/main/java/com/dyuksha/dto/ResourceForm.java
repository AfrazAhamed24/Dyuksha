package com.dyuksha.dto;

import com.dyuksha.model.ResourceType;
import jakarta.validation.constraints.*;

/**
 * Form DTO for registering a new resource or hospital facility.
 */
public class ResourceForm {

    @NotBlank(message = "Resource name is required")
    @Size(min = 2, max = 150, message = "Name must be between 2 and 150 characters")
    private String name;

    @NotNull(message = "Resource type is required")
    private ResourceType type;

    @NotBlank(message = "Agency name is required")
    @Size(max = 150, message = "Agency name must not exceed 150 characters")
    private String agencyName;

    @NotBlank(message = "Current location is required")
    @Size(max = 250, message = "Location must not exceed 250 characters")
    private String currentLocation;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", inclusive = true, message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", inclusive = true, message = "Latitude must be between -90 and 90")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", inclusive = true, message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", inclusive = true, message = "Longitude must be between -180 and 180")
    private Double longitude;

    @Size(max = 500, message = "Capabilities must not exceed 500 characters")
    private String capabilities;

    // Optional / Hospital capacity
    @Min(value = 0, message = "Available beds cannot be negative")
    private Integer availableBeds;

    @Min(value = 0, message = "Total beds cannot be negative")
    private Integer totalBeds;

    public ResourceForm() {}

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ResourceType getType() { return type; }
    public void setType(ResourceType type) { this.type = type; }

    public String getAgencyName() { return agencyName; }
    public void setAgencyName(String agencyName) { this.agencyName = agencyName; }

    public String getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(String currentLocation) { this.currentLocation = currentLocation; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getCapabilities() { return capabilities; }
    public void setCapabilities(String capabilities) { this.capabilities = capabilities; }

    public Integer getAvailableBeds() { return availableBeds; }
    public void setAvailableBeds(Integer availableBeds) { this.availableBeds = availableBeds; }

    public Integer getTotalBeds() { return totalBeds; }
    public void setTotalBeds(Integer totalBeds) { this.totalBeds = totalBeds; }
}

