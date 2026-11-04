package com.dyuksha.service;

import com.dyuksha.dto.AssessmentResult;
import com.dyuksha.model.Emergency;
import com.dyuksha.model.ResourceType;
import com.dyuksha.model.Severity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmergencyAssessmentServiceImpl implements EmergencyAssessmentService {

    @Override
    public AssessmentResult assessEmergency(Emergency emergency) {
        AssessmentResult result = new AssessmentResult();
        result.setEmergencyId(emergency.getId());
        result.setEmergencyTitle(emergency.getTitle());
        result.setEmergencyType(emergency.getType().getDisplayName());
        result.setSeverity(emergency.getSeverity().getDisplayName());

        List<ResourceType> required = new ArrayList<>();
        int estimatedVictims = 0;
        boolean hospitalization = false;
        StringBuilder summary = new StringBuilder();

        switch (emergency.getType()) {
            case FIRE -> {
                required.add(ResourceType.FIRE_AND_RESCUE);
                required.add(ResourceType.AMBULANCE);
                required.add(ResourceType.POLICE_UNIT);
                estimatedVictims = emergency.getSeverity() == Severity.CRITICAL ? 15 : 5;
                hospitalization = true;
                summary.append("Primary threat: Active thermal hazard and structural instability. ")
                       .append("Required: High-volume water/foam engines, smoke ventilation, trauma EMS, and traffic perimeter control.");
            }
            case FLOOD -> {
                required.add(ResourceType.FIRE_AND_RESCUE);
                required.add(ResourceType.POLICE_UNIT);
                required.add(ResourceType.AMBULANCE);
                estimatedVictims = emergency.getSeverity() == Severity.CRITICAL ? 30 : 10;
                hospitalization = emergency.getSeverity() == Severity.CRITICAL || emergency.getSeverity() == Severity.HIGH;
                summary.append("Primary threat: Water ingress and submerged hazards. ")
                       .append("Required: Amphibious rescue rafts, inflatable rescue boats, NDRF squads, evacuation cordoning.");
            }
            case MEDICAL_EMERGENCY -> {
                required.add(ResourceType.AMBULANCE);
                required.add(ResourceType.HOSPITAL);
                estimatedVictims = emergency.getSeverity() == Severity.CRITICAL ? 4 : 1;
                hospitalization = true;
                summary.append("Primary threat: Acute life-threatening trauma or medical condition. ")
                       .append("Required: Mobile ALS/ICU paramedic response and emergency bed staging.");
            }
            case BUILDING_COLLAPSE -> {
                required.add(ResourceType.FIRE_AND_RESCUE);
                required.add(ResourceType.AMBULANCE);
                required.add(ResourceType.POLICE_UNIT);
                required.add(ResourceType.HOSPITAL);
                estimatedVictims = emergency.getSeverity() == Severity.CRITICAL ? 25 : 8;
                hospitalization = true;
                summary.append("Primary threat: Entrapment, crush injury syndrome, void search. ")
                       .append("Required: Heavy hydraulic extraction, canine search squads, multi-patient ambulance corridor.");
            }
            case ROAD_ACCIDENT -> {
                required.add(ResourceType.AMBULANCE);
                required.add(ResourceType.POLICE_UNIT);
                required.add(ResourceType.FIRE_AND_RESCUE);
                estimatedVictims = emergency.getSeverity() == Severity.CRITICAL ? 8 : 3;
                hospitalization = true;
                summary.append("Primary threat: High-speed vehicular collision, extrication and arterial clearance. ")
                       .append("Required: Rapid EMT, cutters for cabin extrication, highway traffic police escort.");
            }
            case OTHER -> {
                required.add(ResourceType.POLICE_UNIT);
                required.add(ResourceType.FIRE_AND_RESCUE);
                required.add(ResourceType.AMBULANCE);
                estimatedVictims = 2;
                summary.append("Primary threat: Industrial hazardous containment / unclassified disturbance. ")
                       .append("Required: Specialized inspection and containment assets.");
            }
        }

        result.setEstimatedVictims(estimatedVictims);
        result.setRequiredResourceTypes(required);
        result.setRequiresHospitalization(hospitalization);
        result.setAssessmentSummary(summary.toString());

        return result;
    }
}

