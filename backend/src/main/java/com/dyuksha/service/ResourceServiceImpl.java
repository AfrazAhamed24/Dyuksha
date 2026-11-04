package com.dyuksha.service;

import com.dyuksha.dto.ResourceForm;
import com.dyuksha.model.*;
import com.dyuksha.repository.EmergencyRepository;
import com.dyuksha.repository.ResourceRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;
    private final EmergencyRepository emergencyRepository;

    public ResourceServiceImpl(ResourceRepository resourceRepository, EmergencyRepository emergencyRepository) {
        this.resourceRepository = resourceRepository;
        this.emergencyRepository = emergencyRepository;
    }

    @Override
    public List<Resource> getAllResources() {
        return resourceRepository.findAllByOrderByIdAsc();
    }

    @Override
    public List<Resource> filter(ResourceType type, ResourceStatus status) {
        return resourceRepository.findAllByOrderByIdAsc().stream()
                .filter(r -> type == null || r.getType() == type)
                .filter(r -> status == null || r.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public Resource getById(Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Resource not found: " + id));
    }

    @Override
    @Transactional
    public Resource create(ResourceForm form) {
        Resource r = new Resource();
        r.setName(form.getName().trim());
        r.setType(form.getType());
        r.setAgencyName(form.getAgencyName().trim());
        r.setCurrentLocation(form.getCurrentLocation().trim());
        r.setLatitude(form.getLatitude());
        r.setLongitude(form.getLongitude());
        r.setCapabilities(form.getCapabilities() != null ? form.getCapabilities().trim() : null);
        r.setStatus(ResourceStatus.AVAILABLE);

        if (form.getType() == ResourceType.HOSPITAL) {
            r.setTotalBeds(form.getTotalBeds() != null ? form.getTotalBeds() : 0);
            r.setAvailableBeds(form.getAvailableBeds() != null ? form.getAvailableBeds() : 0);
            r.updateHospitalCapacityStatus();
        }

        return resourceRepository.save(r);
    }

    @Override
    @Transactional
    public Resource updateStatus(Long id, ResourceStatus newStatus) {
        Resource resource = getById(id);

        if (resource.getStatus() == ResourceStatus.ASSIGNED && newStatus == ResourceStatus.AVAILABLE) {
            throw new IllegalStateException("Assigned resource cannot be marked available directly. Complete assignment through emergency workflow.");
        }

        if (newStatus == ResourceStatus.ASSIGNED && resource.getAssignedEmergencyId() == null) {
            throw new IllegalStateException("Cannot transition to ASSIGNED without an emergency reference.");
        }

        resource.setStatus(newStatus);
        return resourceRepository.save(resource);
    }

    @Override
    @Transactional
    public void assignResourceToEmergency(Long resourceId, Long emergencyId) {
        Resource resource = getById(resourceId);
        Emergency emergency = emergencyRepository.findById(emergencyId)
                .orElseThrow(() -> new EntityNotFoundException("Emergency not found: " + emergencyId));

        if (resource.getStatus() == ResourceStatus.ASSIGNED || resource.getAssignedEmergencyId() != null) {
            throw new IllegalStateException("Resource " + resource.getFormattedId() + " is already assigned to an active incident.");
        }

        resource.setStatus(ResourceStatus.ASSIGNED);
        resource.setAssignedEmergencyId(emergency.getId());
        resourceRepository.save(resource);

        // Update emergency assigned resources string
        String assignedStr = emergency.getAssignedResources();
        if (assignedStr == null || assignedStr.trim().isEmpty() || assignedStr.equalsIgnoreCase("None") || assignedStr.equalsIgnoreCase("None assigned")) {
            assignedStr = resource.getFormattedId();
        } else if (!assignedStr.contains(resource.getFormattedId())) {
            assignedStr += ", " + resource.getFormattedId();
        }
        emergency.setAssignedResources(assignedStr);

        // Update emergency status if it was still in reported/assessing
        if (emergency.getStatus() == EmergencyStatus.REPORTED || emergency.getStatus() == EmergencyStatus.ASSESSING) {
            emergency.setStatus(EmergencyStatus.RESOURCES_ASSIGNED);
        }
        emergencyRepository.save(emergency);
    }

    @Override
    @Transactional
    public void completeResourceAssignment(Long resourceId) {
        Resource resource = getById(resourceId);
        Long emId = resource.getAssignedEmergencyId();

        resource.setStatus(ResourceStatus.AVAILABLE);
        resource.setAssignedEmergencyId(null);
        resourceRepository.save(resource);

        if (emId != null) {
            emergencyRepository.findById(emId).ifPresent(emergency -> {
                // If no remaining resources are assigned to this emergency, consider updating or adjusting status
                List<Resource> remaining = resourceRepository.findByAssignedEmergencyId(emId);
                String updatedRes = remaining.stream().map(Resource::getFormattedId).collect(Collectors.joining(", "));
                emergency.setAssignedResources(updatedRes.isEmpty() ? "None assigned" : updatedRes);
                emergencyRepository.save(emergency);
            });
        }
    }

    @Override
    public long countTotal() {
        return resourceRepository.count();
    }

    @Override
    public long countAvailable() {
        return resourceRepository.countByStatus(ResourceStatus.AVAILABLE);
    }

    @Override
    public long countAssigned() {
        return resourceRepository.countByStatus(ResourceStatus.ASSIGNED);
    }

    @Override
    public long countOffline() {
        return resourceRepository.countByStatus(ResourceStatus.OFFLINE);
    }

    @Override
    public long countAvailableByType(ResourceType type) {
        return resourceRepository.countByTypeAndStatus(type, ResourceStatus.AVAILABLE);
    }

    @Override
    public Map<String, List<Map<String, Object>>> getGroupedResourcesForDashboard() {
        Map<String, List<Map<String, Object>>> map = new LinkedHashMap<>();
        map.put("ambulances", new ArrayList<>());
        map.put("fire", new ArrayList<>());
        map.put("police", new ArrayList<>());
        map.put("hospitals", new ArrayList<>());

        for (Resource r : getAllResources()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", r.getFormattedId());
            item.put("name", r.getName());
            item.put("agency", r.getAgencyName());
            item.put("status", r.getStatus().getCssClass());
            item.put("location", r.getCurrentLocation());
            item.put("lat", r.getLatitude());
            item.put("lng", r.getLongitude());

            if (r.getType() == ResourceType.HOSPITAL) {
                item.put("availableBeds", r.getAvailableBeds());
                item.put("totalBeds", r.getTotalBeds());
                item.put("capacityStatus", r.getCapacityStatus() != null ? r.getCapacityStatus().getDisplayName() : "Normal");
            }

            String cat = r.getType().getCategoryKey();
            if (map.containsKey(cat)) {
                map.get(cat).add(item);
            }
        }
        return map;
    }

    @Override
    public List<Map<String, Object>> getActiveForMap() {
        return resourceRepository.findAll().stream()
                .filter(r -> r.getStatus() != ResourceStatus.OFFLINE)
                .filter(r -> r.getLatitude() != null && r.getLongitude() != null)
                .map(r -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", r.getFormattedId());
                    item.put("name", r.getName());
                    item.put("type", r.getType().name());
                    item.put("category", r.getType().getCategoryKey());
                    item.put("agency", r.getAgencyName());
                    item.put("status", r.getStatus().getCssClass());
                    item.put("location", r.getCurrentLocation());
                    item.put("lat", r.getLatitude());
                    item.put("lng", r.getLongitude());
                    if (r.getType() == ResourceType.HOSPITAL) {
                        item.put("availableBeds", r.getAvailableBeds());
                        item.put("totalBeds", r.getTotalBeds());
                    }
                    return item;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Resource> getAssignedToEmergency(Long emergencyId) {
        return resourceRepository.findByAssignedEmergencyId(emergencyId);
    }
}

