package com.dyuksha.dto;

import com.dyuksha.model.Resource;

/**
 * An individual resource ranked recommendation with weighted scoring breakdown.
 */
public class ResourceRecommendation {

    private Resource resource;
    private double distanceKm;
    private double totalScore; // 0 to 100

    // Component scores
    private double suitabilityScore; // weight 40%
    private double distanceScore;    // weight 30%
    private double severityScore;    // weight 20%
    private double availabilityScore;// weight 10%

    private String explanation;
    private boolean recommended;

    public ResourceRecommendation() {}

    public Resource getResource() { return resource; }
    public void setResource(Resource resource) { this.resource = resource; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public double getTotalScore() { return totalScore; }
    public void setTotalScore(double totalScore) { this.totalScore = totalScore; }

    public double getSuitabilityScore() { return suitabilityScore; }
    public void setSuitabilityScore(double suitabilityScore) { this.suitabilityScore = suitabilityScore; }

    public double getDistanceScore() { return distanceScore; }
    public void setDistanceScore(double distanceScore) { this.distanceScore = distanceScore; }

    public double getSeverityScore() { return severityScore; }
    public void setSeverityScore(double severityScore) { this.severityScore = severityScore; }

    public double getAvailabilityScore() { return availabilityScore; }
    public void setAvailabilityScore(double availabilityScore) { this.availabilityScore = availabilityScore; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public boolean isRecommended() { return recommended; }
    public void setRecommended(boolean recommended) { this.recommended = recommended; }

    public int getFormattedScore() {
        return (int) Math.round(totalScore);
    }

    public String getFormattedDistance() {
        return String.format("%.1f km", distanceKm);
    }
}

