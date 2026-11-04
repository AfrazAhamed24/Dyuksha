package com.dyuksha.dto;

import com.dyuksha.model.ResourceType;
import java.util.List;

/**
 * Result of analyzing an incident to determine operational resource requirements.
 */
public class AssessmentResult {

    private Long emergencyId;
    private String emergencyTitle;
    private String emergencyType;
    private String severity;
    private int estimatedVictims;
    private List<ResourceType> requiredResourceTypes;
    private String assessmentSummary;
    private boolean requiresHospitalization;

    public AssessmentResult() {}

    public Long getEmergencyId() { return emergencyId; }
    public void setEmergencyId(Long emergencyId) { this.emergencyId = emergencyId; }

    public String getEmergencyTitle() { return emergencyTitle; }
    public void setEmergencyTitle(String emergencyTitle) { this.emergencyTitle = emergencyTitle; }

    public String getEmergencyType() { return emergencyType; }
    public void setEmergencyType(String emergencyType) { this.emergencyType = emergencyType; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public int getEstimatedVictims() { return estimatedVictims; }
    public void setEstimatedVictims(int estimatedVictims) { this.estimatedVictims = estimatedVictims; }

    public List<ResourceType> getRequiredResourceTypes() { return requiredResourceTypes; }
    public void setRequiredResourceTypes(List<ResourceType> requiredResourceTypes) { this.requiredResourceTypes = requiredResourceTypes; }

    public String getAssessmentSummary() { return assessmentSummary; }
    public void setAssessmentSummary(String assessmentSummary) { this.assessmentSummary = assessmentSummary; }

    public boolean isRequiresHospitalization() { return requiresHospitalization; }
    public void setRequiresHospitalization(boolean requiresHospitalization) { this.requiresHospitalization = requiresHospitalization; }
}

