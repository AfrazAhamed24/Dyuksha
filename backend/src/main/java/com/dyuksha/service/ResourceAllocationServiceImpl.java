package com.dyuksha.service;

import com.dyuksha.dto.AssessmentResult;
import com.dyuksha.dto.ResourceRecommendation;
import com.dyuksha.model.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ResourceAllocationServiceImpl implements ResourceAllocationService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    // Weights: Suitability 40%, Distance 30%, Severity 20%, Availability 10%
    private static final double WEIGHT_SUITABILITY  = 0.40;
    private static final double WEIGHT_DISTANCE     = 0.30;
    private static final double WEIGHT_SEVERITY     = 0.20;
    private static final double WEIGHT_AVAILABILITY = 0.10;

    @Override
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    @Override
    public List<ResourceRecommendation> rankResources(
            Emergency emergency,
            AssessmentResult assessment,
            List<Resource> candidates) {

        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }

        double emLat = emergency.getLatitude() != null ? emergency.getLatitude() : 12.9716;
        double emLng = emergency.getLongitude() != null ? emergency.getLongitude() : 77.5946;

        List<ResourceRecommendation> recommendations = new ArrayList<>();

        for (Resource r : candidates) {
            // Calculate distance via Haversine
            double resLat = r.getLatitude() != null ? r.getLatitude() : emLat;
            double resLng = r.getLongitude() != null ? r.getLongitude() : emLng;
            double distanceKm = calculateDistance(emLat, emLng, resLat, resLng);

            // 1. Suitability Score (0-100)
            double suitability = calculateSuitability(r, emergency, assessment);

            // 2. Distance Score (0-100): 100 within 2km, dropping smoothly as distance increases up to 35km
            double distanceScore = Math.max(0.0, 100.0 - (distanceKm * 3.5));

            // 3. Severity Match Score (0-100)
            double severityScore = calculateSeverityScore(r, emergency.getSeverity());

            // 4. Availability Score (0-100)
            double availabilityScore = calculateAvailabilityScore(r);

            // Total Weighted Score
            double totalScore = (suitability * WEIGHT_SUITABILITY)
                              + (distanceScore * WEIGHT_DISTANCE)
                              + (severityScore * WEIGHT_SEVERITY)
                              + (availabilityScore * WEIGHT_AVAILABILITY);

            String explanation = buildExplanation(r, suitability, distanceKm, distanceScore, totalScore);

            ResourceRecommendation rec = new ResourceRecommendation();
            rec.setResource(r);
            rec.setDistanceKm(distanceKm);
            rec.setSuitabilityScore(suitability);
            rec.setDistanceScore(distanceScore);
            rec.setSeverityScore(severityScore);
            rec.setAvailabilityScore(availabilityScore);
            rec.setTotalScore(Math.min(100.0, Math.max(0.0, totalScore)));
            rec.setExplanation(explanation);
            rec.setRecommended(rec.getTotalScore() >= 65.0);

            recommendations.add(rec);
        }

        // Rank by highest total score first
        recommendations.sort(Comparator.comparingDouble(ResourceRecommendation::getTotalScore).reversed());

        return recommendations;
    }

    private double calculateSuitability(Resource r, Emergency emergency, AssessmentResult assessment) {
        double score = 50.0; // base

        boolean isDirectlyRequired = assessment.getRequiredResourceTypes().contains(r.getType());
        if (isDirectlyRequired) {
            score += 30.0;
        } else {
            score -= 20.0;
        }

        // Capability textual keyword match against incident
        String cap = r.getCapabilities() != null ? r.getCapabilities().toLowerCase() : "";
        String emText = (emergency.getTitle() + " " + (emergency.getDescription() != null ? emergency.getDescription() : "")).toLowerCase();

        if (emergency.getType() == EmergencyType.FIRE && (cap.contains("water") || cap.contains("foam") || cap.contains("ladder"))) {
            score += 20.0;
        }
        if (emergency.getType() == EmergencyType.FLOOD && (cap.contains("boat") || cap.contains("raft") || cap.contains("usar"))) {
            score += 20.0;
        }
        if (emergency.getType() == EmergencyType.MEDICAL_EMERGENCY && (cap.contains("icu") || cap.contains("cardiac") || cap.contains("trauma"))) {
            score += 20.0;
        }
        if (emergency.getType() == EmergencyType.BUILDING_COLLAPSE && (cap.contains("canine") || cap.contains("usar") || cap.contains("cutter"))) {
            score += 20.0;
        }

        // Hospital capacity penalty/bonus
        if (r.getType() == ResourceType.HOSPITAL) {
            if (r.getCapacityStatus() == HospitalCapacityStatus.NORMAL) {
                score += 15.0;
            } else if (r.getCapacityStatus() == HospitalCapacityStatus.SURGE) {
                score += 5.0;
            } else if (r.getCapacityStatus() == HospitalCapacityStatus.CRITICAL) {
                score -= 25.0;
            } else if (r.getCapacityStatus() == HospitalCapacityStatus.DIVERT) {
                score -= 60.0;
            }
        }

        return Math.min(100.0, Math.max(0.0, score));
    }

    private double calculateSeverityScore(Resource r, Severity severity) {
        return switch (severity) {
            case CRITICAL -> {
                String cap = r.getCapabilities() != null ? r.getCapabilities().toLowerCase() : "";
                yield (cap.contains("icu") || cap.contains("als") || cap.contains("usar") || cap.contains("aerial")) ? 100.0 : 80.0;
            }
            case HIGH -> 85.0;
            case MEDIUM -> 70.0;
            case LOW -> 55.0;
        };
    }

    private double calculateAvailabilityScore(Resource r) {
        if (r.getStatus() == ResourceStatus.AVAILABLE) return 100.0;
        if (r.getStatus() == ResourceStatus.BUSY) return 40.0;
        return 0.0;
    }

    private String buildExplanation(Resource r, double suitability, double distanceKm, double distanceScore, double totalScore) {
        StringBuilder sb = new StringBuilder();
        if (r.getType() == ResourceType.HOSPITAL) {
            sb.append(String.format("Facility staging %s with %d beds free. ",
                    r.getCapacityStatus() != null ? r.getCapacityStatus().getDisplayName() : "Normal",
                    r.getAvailableBeds() != null ? r.getAvailableBeds() : 0));
        } else {
            sb.append(String.format("Assigned agency: %s. ", r.getAgencyName()));
        }

        if (distanceKm <= 3.0) {
            sb.append(String.format("Immediate proximity (%.1f km). ", distanceKm));
        } else if (distanceKm <= 8.0) {
            sb.append(String.format("Short transit ETA (%.1f km). ", distanceKm));
        } else {
            sb.append(String.format("Moderate distance (%.1f km). ", distanceKm));
        }

        if (suitability >= 80.0) {
            sb.append("High capability alignment with incident profile.");
        } else if (suitability >= 60.0) {
            sb.append("Adequate operational capability.");
        } else {
            sb.append("Secondary support profile.");
        }

        return sb.toString();
    }
}

