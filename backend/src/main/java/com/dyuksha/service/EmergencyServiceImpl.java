package com.dyuksha.service;

import com.dyuksha.dto.EmergencyForm;
import com.dyuksha.model.*;
import com.dyuksha.repository.ActivityLogRepository;
import com.dyuksha.repository.EmergencyRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link EmergencyService}.
 * All database operations go through the repository;
 * filtering and mapping are handled here in the service layer.
 */
@Service
@Transactional(readOnly = true)
public class EmergencyServiceImpl implements EmergencyService {

    private final EmergencyRepository repository;
    private final ActivityLogRepository activityLogRepository;

    public EmergencyServiceImpl(EmergencyRepository repository, ActivityLogRepository activityLogRepository) {
        this.repository = repository;
        this.activityLogRepository = activityLogRepository;
    }

    // -------------------------------------------------------------------------
    // Query operations
    // -------------------------------------------------------------------------

    @Override
    public List<Emergency> getAllEmergencies() {
        return repository.findAllByOrderByReportedAtDesc();
    }

    @Override
    public List<Emergency> filter(EmergencyType type, Severity severity, EmergencyStatus status) {
        return repository.findAllByOrderByReportedAtDesc().stream()
                .filter(e -> type     == null || e.getType()     == type)
                .filter(e -> severity == null || e.getSeverity() == severity)
                .filter(e -> status   == null || e.getStatus()   == status)
                .collect(Collectors.toList());
    }

    @Override
    public Emergency getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Emergency not found: " + id));
    }

    @Override
    public long countActive() {
        return repository.countByStatusNot(EmergencyStatus.RESOLVED);
    }

    @Override
    public long countCritical() {
        return repository.countBySeverityAndStatusNot(Severity.CRITICAL, EmergencyStatus.RESOLVED);
    }

    @Override
    public List<Map<String, Object>> getActiveForMap() {
        return repository.findAllByOrderByReportedAtDesc().stream()
                .filter(Emergency::isActive)
                .filter(e -> e.getLatitude() != null && e.getLongitude() != null)
                .map(this::toMapPoint)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getRecentActivity(int limit) {
        List<ActivityLog> logs = activityLogRepository.findTop15ByOrderByTimestampDesc();
        if (!logs.isEmpty()) {
            return logs.stream().limit(limit).map(l -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("severity", l.getSeverity());
                m.put("text", l.getMessage());
                long mins = ChronoUnit.MINUTES.between(l.getTimestamp(), LocalDateTime.now());
                String timeAgo = mins < 1 ? "Just now" : (mins < 60 ? mins + " min ago" : (mins / 60) + " hr ago");
                m.put("time", timeAgo);
                return m;
            }).collect(Collectors.toList());
        }

        // Fallback to recent emergencies if activity logs are empty
        return repository.findTop10ByOrderByReportedAtDesc().stream()
                .limit(limit)
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("severity", e.getSeverity().getCssClass());
                    m.put("text", String.format(
                            "<strong>%s</strong> %s &mdash; %s",
                            e.getFormattedId(), e.getTitle(), e.getLocationName()));
                    m.put("time", e.getTimeAgo());
                    return m;
                })
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Write operations
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public Emergency create(EmergencyForm form) {
        Emergency e = new Emergency();
        e.setTitle(form.getTitle().trim());
        e.setType(form.getType());
        e.setDescription(form.getDescription() != null ? form.getDescription().trim() : null);
        e.setLocationName(form.getLocationName().trim());
        e.setLatitude(form.getLatitude());
        e.setLongitude(form.getLongitude());
        e.setSeverity(form.getSeverity());
        e.setStatus(EmergencyStatus.REPORTED);
        e.setReportedAt(LocalDateTime.now());
        Emergency saved = repository.save(e);

        activityLogRepository.save(new ActivityLog(saved.getSeverity().getCssClass(),
                String.format("New Emergency Reported: %s (%s) at %s", saved.getFormattedId(), saved.getTitle(), saved.getLocationName())));

        return saved;
    }

    @Override
    @Transactional
    public Emergency updateStatus(Long id, EmergencyStatus newStatus) {
        Emergency e = getById(id);
        EmergencyStatus oldStatus = e.getStatus();
        e.setStatus(newStatus);
        Emergency saved = repository.save(e);

        activityLogRepository.save(new ActivityLog("info",
                String.format("Incident %s status changed from %s to %s", saved.getFormattedId(), oldStatus.getDisplayName(), newStatus.getDisplayName())));

        return saved;
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private Map<String, Object> toMapPoint(Emergency e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",           e.getFormattedId());
        m.put("title",        e.getTitle());
        m.put("type",         e.getType().getDisplayName());
        m.put("locationName", e.getLocationName());
        m.put("severity",     e.getSeverity().getCssClass());           // "critical", "high", ...
        m.put("status",       e.getStatus().getCssClass());             // "reported", "response-in-progress", ...
        m.put("statusLabel",  e.getStatus().getDisplayName());
        m.put("lat",          e.getLatitude());
        m.put("lng",          e.getLongitude());
        m.put("reported",     e.getTimeAgo());
        m.put("responders",   0);
        return m;
    }
}

