package com.dyuksha.service;

import com.dyuksha.dto.EmergencyForm;
import com.dyuksha.model.Emergency;
import com.dyuksha.model.EmergencyStatus;
import com.dyuksha.model.EmergencyType;
import com.dyuksha.model.Severity;

import java.util.List;
import java.util.Map;

/**
 * Business-logic facade for emergency incident management.
 */
public interface EmergencyService {

    /** Return all emergencies, newest-first. */
    List<Emergency> getAllEmergencies();

    /**
     * Filter emergencies by optional type, severity and status.
     * Any null parameter acts as "match all".
     */
    List<Emergency> filter(EmergencyType type, Severity severity, EmergencyStatus status);

    /** Find by primary key. Throws {@link jakarta.persistence.EntityNotFoundException} if missing. */
    Emergency getById(Long id);

    /** Persist a new emergency from the validated form. */
    Emergency create(EmergencyForm form);

    /** Update only the status of an existing emergency. */
    Emergency updateStatus(Long id, EmergencyStatus newStatus);

    /** Count emergencies that are NOT resolved. */
    long countActive();

    /** Count CRITICAL emergencies that are NOT resolved. */
    long countCritical();

    /**
     * Return active emergencies as lightweight maps suitable for
     * Thymeleaf JavaScript inlining and Leaflet map rendering.
     */
    List<Map<String, Object>> getActiveForMap();

    /**
     * Return the most recent {@code limit} emergencies as activity-feed maps.
     * Each map has keys: severity, text (HTML), time.
     */
    List<Map<String, Object>> getRecentActivity(int limit);
}

