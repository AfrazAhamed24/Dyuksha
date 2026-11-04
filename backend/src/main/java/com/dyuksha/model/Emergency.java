package com.dyuksha.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * JPA entity representing a reported emergency incident.
 */
@Entity
@Table(name = "emergencies")
public class Emergency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmergencyType type;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, length = 300)
    private String locationName;

    private Double latitude;

    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmergencyStatus status;

    @Column(nullable = false)
    private LocalDateTime reportedAt;

    /** Free-text field for assigned resource IDs (Phase 4 will make this relational). */
    @Column(length = 500)
    private String assignedResources;

    // -------------------------------------------------------------------------
    // Lifecycle callbacks
    // -------------------------------------------------------------------------

    @PrePersist
    protected void onCreate() {
        if (reportedAt == null) {
            reportedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = EmergencyStatus.REPORTED;
        }
    }

    // -------------------------------------------------------------------------
    // Computed / transient helpers (not persisted)
    // -------------------------------------------------------------------------

    /** Human-readable ID, e.g. "EM-007". */
    @Transient
    public String getFormattedId() {
        return id != null ? String.format("EM-%03d", id) : "EM-???";
    }

    /** Relative time string, e.g. "12 min ago". */
    @Transient
    public String getTimeAgo() {
        if (reportedAt == null) return "Unknown";
        long mins = ChronoUnit.MINUTES.between(reportedAt, LocalDateTime.now());
        if (mins < 1)  return "Just now";
        if (mins < 60) return mins + " min ago";
        long hrs = mins / 60;
        if (hrs < 24)  return hrs + " hr ago";
        long days = hrs / 24;
        return days + " day" + (days > 1 ? "s" : "") + " ago";
    }

    /** Whether this emergency is considered active (not resolved). */
    @Transient
    public boolean isActive() {
        return status != null && status != EmergencyStatus.RESOLVED;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public Emergency() {}

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public EmergencyStatus getStatus() { return status; }
    public void setStatus(EmergencyStatus status) { this.status = status; }

    public LocalDateTime getReportedAt() { return reportedAt; }
    public void setReportedAt(LocalDateTime reportedAt) { this.reportedAt = reportedAt; }

    public String getAssignedResources() { return assignedResources; }
    public void setAssignedResources(String assignedResources) { this.assignedResources = assignedResources; }
}

