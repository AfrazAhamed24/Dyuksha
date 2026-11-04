package com.dyuksha.service;

import com.dyuksha.model.ActivityLog;
import com.dyuksha.model.Emergency;
import com.dyuksha.model.Resource;
import com.dyuksha.repository.ActivityLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgencyCoordinationServiceImpl implements AgencyCoordinationService {

    private static final Logger log = LoggerFactory.getLogger(AgencyCoordinationServiceImpl.class);

    private final ActivityLogRepository activityLogRepository;

    public AgencyCoordinationServiceImpl(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    @Override
    @Transactional
    public void notifyAgencyDispatch(Emergency emergency, Resource resource) {
        String msg = String.format("AI Allocation Approved: Dispatched %s (%s) from %s to incident %s — %s",
                resource.getFormattedId(), resource.getName(), resource.getAgencyName(),
                emergency.getFormattedId(), emergency.getTitle());
        log.info("[AGENCY COORDINATION] {}", msg);

        activityLogRepository.save(new ActivityLog(emergency.getSeverity().getCssClass(), msg));
    }

    @Override
    @Transactional
    public void notifyAssignmentRelease(Emergency emergency, Resource resource) {
        String msg = String.format("Coordination Release: %s (%s) completed task at %s and returned to Available status",
                resource.getFormattedId(), resource.getName(),
                emergency != null ? emergency.getFormattedId() : "Incident");
        log.info("[AGENCY COORDINATION] {}", msg);

        activityLogRepository.save(new ActivityLog("info", msg));
    }

    @Override
    @Transactional
    public void logCoordinationEvent(String severity, String message) {
        activityLogRepository.save(new ActivityLog(severity, message));
    }
}

