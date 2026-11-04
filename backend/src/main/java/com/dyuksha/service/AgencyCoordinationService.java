package com.dyuksha.service;

import com.dyuksha.model.Emergency;
import com.dyuksha.model.Resource;

/**
 * Service representing the Agency Coordination Agent.
 * Handles dispatch notifications, inter-agency agreements, and activity logging.
 */
public interface AgencyCoordinationService {

    void notifyAgencyDispatch(Emergency emergency, Resource resource);

    void notifyAssignmentRelease(Emergency emergency, Resource resource);

    void logCoordinationEvent(String severity, String message);
}

