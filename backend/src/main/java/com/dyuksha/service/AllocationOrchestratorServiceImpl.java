package com.dyuksha.service;

import com.dyuksha.dto.AllocationPlan;
import com.dyuksha.dto.AssessmentResult;
import com.dyuksha.dto.ResourceRecommendation;
import com.dyuksha.model.*;
import com.dyuksha.repository.EmergencyRepository;
import com.dyuksha.repository.ResourceRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AllocationOrchestratorServiceImpl implements AllocationOrchestratorService {

    private final EmergencyRepository emergencyRepository;
    private final ResourceRepository resourceRepository;
    private final EmergencyAssessmentService assessmentService;
    private final ResourceAllocationService allocationService;
    private final AgencyCoordinationService coordinationService;

    public AllocationOrchestratorServiceImpl(
            EmergencyRepository emergencyRepository,
            ResourceRepository resourceRepository,
            EmergencyAssessmentService assessmentService,
            ResourceAllocationService allocationService,
            AgencyCoordinationService coordinationService) {
        this.emergencyRepository = emergencyRepository;
        this.resourceRepository = resourceRepository;
        this.assessmentService = assessmentService;
        this.allocationService = allocationService;
        this.coordinationService = coordinationService;
    }

    @Override
    @Transactional(readOnly = true)
    public AllocationPlan generatePlan(Long emergencyId) {
        Emergency emergency = emergencyRepository.findById(emergencyId)
                .orElseThrow(() -> new EntityNotFoundException("Emergency not found: " + emergencyId));

        if (!emergency.isActive()) {
            throw new IllegalStateException("Cannot generate resource recommendations for a resolved emergency (" + emergency.getFormattedId() + ")");
        }

        // 1. Multi-Agent Assessment Step
        AssessmentResult assessment = assessmentService.assessEmergency(emergency);

        // 2. Fetch candidate resources (Available or Busy hospitals that have beds)
        List<Resource> availableResources = resourceRepository.findAll().stream()
                .filter(r -> r.getStatus() == ResourceStatus.AVAILABLE ||
                        (r.getType() == ResourceType.HOSPITAL && r.getAvailableBeds() != null && r.getAvailableBeds() > 0 && r.getStatus() != ResourceStatus.OFFLINE))
                .collect(Collectors.toList());

        // 3. Multi-Agent Allocation / Ranking Step
        List<ResourceRecommendation> ranked = allocationService.rankResources(emergency, assessment, availableResources);

        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        coordinationService.logCoordinationEvent("info",
                String.format("AI Allocation Engine evaluated %d available assets for incident %s (%s)",
                        availableResources.size(), emergency.getFormattedId(), emergency.getTitle()));

        return new AllocationPlan(emergency, assessment, ranked, time);
    }

    @Override
    @Transactional
    public void approveAssignment(Long emergencyId, Long resourceId) {
        Emergency emergency = emergencyRepository.findById(emergencyId)
                .orElseThrow(() -> new EntityNotFoundException("Emergency not found: " + emergencyId));

        if (!emergency.isActive()) {
            throw new IllegalStateException("Emergency " + emergency.getFormattedId() + " is already resolved. Cannot assign resources.");
        }

        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new EntityNotFoundException("Resource not found: " + resourceId));

        // Strict verification: Resource must be available (or not assigned elsewhere)
        if (resource.getStatus() == ResourceStatus.ASSIGNED || resource.getAssignedEmergencyId() != null) {
            throw new IllegalStateException("Assignment Rejected: Resource " + resource.getFormattedId() +
                    " (" + resource.getName() + ") has already been assigned to another incident. Please regenerate recommendations.");
        }

        if (resource.getStatus() == ResourceStatus.OFFLINE) {
            throw new IllegalStateException("Assignment Rejected: Resource " + resource.getFormattedId() + " is currently OFFLINE.");
        }

        // 1. Assign Resource
        resource.setStatus(ResourceStatus.ASSIGNED);
        resource.setAssignedEmergencyId(emergency.getId());
        if (resource.getType() == ResourceType.HOSPITAL && resource.getAvailableBeds() != null && resource.getAvailableBeds() > 0) {
            resource.setAvailableBeds(resource.getAvailableBeds() - 1);
            resource.updateHospitalCapacityStatus();
        }
        resourceRepository.save(resource);

        // 2. Update Emergency
        String currentAssigned = emergency.getAssignedResources();
        if (currentAssigned == null || currentAssigned.isBlank() || currentAssigned.equalsIgnoreCase("None") || currentAssigned.equalsIgnoreCase("None assigned")) {
            currentAssigned = resource.getFormattedId();
        } else if (!currentAssigned.contains(resource.getFormattedId())) {
            currentAssigned += ", " + resource.getFormattedId();
        }
        emergency.setAssignedResources(currentAssigned);

        if (emergency.getStatus() == EmergencyStatus.REPORTED || emergency.getStatus() == EmergencyStatus.ASSESSING) {
            emergency.setStatus(EmergencyStatus.RESOURCES_ASSIGNED);
        }
        emergencyRepository.save(emergency);

        // 3. Multi-Agency Coordination & Audit Log
        coordinationService.notifyAgencyDispatch(emergency, resource);
    }

    @Override
    @Transactional
    public void rejectRecommendation(Long emergencyId, Long resourceId) {
        Emergency emergency = emergencyRepository.findById(emergencyId).orElse(null);
        Resource resource = resourceRepository.findById(resourceId).orElse(null);

        String emId = emergency != null ? emergency.getFormattedId() : "EM-" + emergencyId;
        String resName = resource != null ? resource.getFormattedId() + " (" + resource.getName() + ")" : "RES-" + resourceId;

        coordinationService.logCoordinationEvent("medium",
                String.format("Operator Override: Recommendation for %s on incident %s was rejected. Asset remains in staging.",
                        resName, emId));
    }
}

