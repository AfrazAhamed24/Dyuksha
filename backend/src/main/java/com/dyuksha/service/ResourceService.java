package com.dyuksha.service;

import com.dyuksha.dto.ResourceForm;
import com.dyuksha.model.Resource;
import com.dyuksha.model.ResourceStatus;
import com.dyuksha.model.ResourceType;

import java.util.List;
import java.util.Map;

/**
 * Service contract for managing operational emergency resources and facility capacity.
 */
public interface ResourceService {

    List<Resource> getAllResources();

    List<Resource> filter(ResourceType type, ResourceStatus status);

    Resource getById(Long id);

    Resource create(ResourceForm form);

    Resource updateStatus(Long id, ResourceStatus newStatus);

    void assignResourceToEmergency(Long resourceId, Long emergencyId);

    void completeResourceAssignment(Long resourceId);

    long countTotal();

    long countAvailable();

    long countAssigned();

    long countOffline();

    long countAvailableByType(ResourceType type);

    Map<String, List<Map<String, Object>>> getGroupedResourcesForDashboard();

    List<Map<String, Object>> getActiveForMap();

    List<Resource> getAssignedToEmergency(Long emergencyId);
}

