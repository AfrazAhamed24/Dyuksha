package com.dyuksha;

import com.dyuksha.dto.AllocationPlan;
import com.dyuksha.dto.ResourceRecommendation;
import com.dyuksha.model.*;
import com.dyuksha.repository.EmergencyRepository;
import com.dyuksha.repository.ResourceRepository;
import com.dyuksha.service.AllocationOrchestratorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AllocationEngineTests {

    @Autowired
    private AllocationOrchestratorService orchestratorService;

    @Autowired
    private EmergencyRepository emergencyRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Test
    @DisplayName("Generate recommendation plan with ranking, scores, and explanations")
    void testPlanGeneration() {
        List<Emergency> emergencies = emergencyRepository.findAllByOrderByReportedAtDesc();
        assertFalse(emergencies.isEmpty());
        Emergency target = emergencies.getFirst();

        AllocationPlan plan = orchestratorService.generatePlan(target.getId());

        assertNotNull(plan);
        assertNotNull(plan.getAssessment());
        assertFalse(plan.getRecommendations().isEmpty());

        ResourceRecommendation topRec = plan.getRecommendations().getFirst();
        assertTrue(topRec.getTotalScore() >= 0.0 && topRec.getTotalScore() <= 100.0);
        assertTrue(topRec.getDistanceKm() >= 0.0);
        assertNotNull(topRec.getExplanation());
    }

    @Test
    @Transactional
    @DisplayName("Approve assignment transitions resource to ASSIGNED and prevents duplicate active assignment")
    void testAssignmentAndDuplicatePrevention() {
        List<Emergency> emergencies = emergencyRepository.findAllByOrderByReportedAtDesc();
        Emergency target = emergencies.getFirst();

        AllocationPlan plan = orchestratorService.generatePlan(target.getId());
        Resource candidate = plan.getRecommendations().getFirst().getResource();

        // 1. Approve assignment
        orchestratorService.approveAssignment(target.getId(), candidate.getId());

        Resource updatedResource = resourceRepository.findById(candidate.getId()).orElseThrow();
        assertEquals(ResourceStatus.ASSIGNED, updatedResource.getStatus());
        assertEquals(target.getId(), updatedResource.getAssignedEmergencyId());

        // 2. Duplicate assignment prevention test: assigning the same resource to another incident must fail
        Emergency secondTarget = emergencies.size() > 1 ? emergencies.get(1) : target;
        assertThrows(IllegalStateException.class, () -> {
            orchestratorService.approveAssignment(secondTarget.getId(), candidate.getId());
        });
    }

    @Test
    @DisplayName("Rejecting a recommendation leaves the resource Available and unassigned")
    void testRejectRecommendation() {
        List<Emergency> emergencies = emergencyRepository.findAllByOrderByReportedAtDesc();
        Emergency target = emergencies.getFirst();

        AllocationPlan plan = orchestratorService.generatePlan(target.getId());
        Resource candidate = plan.getRecommendations().getFirst().getResource();
        ResourceStatus initialStatus = candidate.getStatus();

        orchestratorService.rejectRecommendation(target.getId(), candidate.getId());

        Resource reloaded = resourceRepository.findById(candidate.getId()).orElseThrow();
        assertEquals(initialStatus, reloaded.getStatus());
        assertNull(reloaded.getAssignedEmergencyId());
    }
}

