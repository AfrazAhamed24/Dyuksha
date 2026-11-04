package com.dyuksha.service;

import com.dyuksha.dto.AllocationPlan;

/**
 * Orchestrator service coordinating multi-agent allocation tasks, operator approval workflows,
 * and state transitions.
 */
public interface AllocationOrchestratorService {

    AllocationPlan generatePlan(Long emergencyId);

    void approveAssignment(Long emergencyId, Long resourceId);

    void rejectRecommendation(Long emergencyId, Long resourceId);
}

