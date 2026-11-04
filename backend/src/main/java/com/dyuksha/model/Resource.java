package com.dyuksha.model;

import jakarta.persistence.*;

/**
 * JPA entity representing a multi-agency resource or healthcare facility.
 */
@Entity
@Table(name = "resources")
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceType type;

    @Column(nullable = false, length = 150)
    private String agencyName;

    @Column(nullable = false, length = 250)
    private String currentLocation;

    private Double latitude;

    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceStatus status;

    @Column(length = 500)
    private String capabilities;

    /** Currently assigned emergency ID, or null if unassigned. */
    private Long assignedEmergencyId;

    // --- Hospital specific metrics ---
    private Integer availableBeds;
    private Integer totalBeds;

    @Enumerated(EnumType.STRING)
    private HospitalCapacityStatus capacityStatus;

    public Resource() {
        this.status = ResourceStatus.AVAILABLE;
    }

    @PrePersist
    @PreUpdate
    protected void onPersistOrUpdate() {
        if (type == ResourceType.HOSPITAL) {
            updateHospitalCapacityStatus();
        }
    }

    public void updateHospitalCapacityStatus() {
        if (totalBeds == null || totalBeds <= 0 || availableBeds == null) {
            this.capacityStatus = HospitalCapacityStatus.NORMAL;
            return;
        }
        double ratio = (double) availableBeds / totalBeds;
        if (ratio <= 0.05) {
            this.capacityStatus = HospitalCapacityStatus.DIVERT;
        } else if (ratio <= 0.20) {
            this.capacityStatus = HospitalCapacityStatus.CRITICAL;
        } else if (ratio <= 0.40) {
            this.capacityStatus = HospitalCapacityStatus.SURGE;
        } else {
            this.capacityStatus = HospitalCapacityStatus.NORMAL;
        }
    }

    @Transient
    public String getFormattedId() {
        if (id == null) return "RES-???";
        String prefix = switch (type) {
            case AMBULANCE -> "AMB";
            case FIRE_AND_RESCUE -> "FR";
            case POLICE_UNIT -> "PC";
            case HOSPITAL -> "HSP";
        };
        return String.format("%s-%02d", prefix, id);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public ResourceStatus getStatus() { return status; }
    public void setStatus(ResourceStatus status) { this.status = status; }

    public String getCapabilities() { return capabilities; }
    public void setCapabilities(String capabilities) { this.capabilities = capabilities; }

    public Long getAssignedEmergencyId() { return assignedEmergencyId; }
    public void setAssignedEmergencyId(Long assignedEmergencyId) { this.assignedEmergencyId = assignedEmergencyId; }

    public Integer getAvailableBeds() { return availableBeds; }
    public void setAvailableBeds(Integer availableBeds) { this.availableBeds = availableBeds; }

    public Integer getTotalBeds() { return totalBeds; }
    public void setTotalBeds(Integer totalBeds) { this.totalBeds = totalBeds; }

    public HospitalCapacityStatus getCapacityStatus() { return capacityStatus; }
    public void setCapacityStatus(HospitalCapacityStatus capacityStatus) { this.capacityStatus = capacityStatus; }
}

