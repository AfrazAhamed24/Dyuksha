package com.dyuksha.dto;

import com.dyuksha.model.Emergency;
import java.util.List;

/**
 * Complete allocation plan generated for an emergency.
 */
public class AllocationPlan {

    private Emergency emergency;
    private AssessmentResult assessment;
    private List<ResourceRecommendation> recommendations;
    private String generatedAt;

    public AllocationPlan() {}

    public AllocationPlan(Emergency emergency, AssessmentResult assessment, List<ResourceRecommendation> recommendations, String generatedAt) {
        this.emergency = emergency;
        this.assessment = assessment;
        this.recommendations = recommendations;
        this.generatedAt = generatedAt;
    }

    public Emergency getEmergency() { return emergency; }
    public void setEmergency(Emergency emergency) { this.emergency = emergency; }

    public AssessmentResult getAssessment() { return assessment; }
    public void setAssessment(AssessmentResult assessment) { this.assessment = assessment; }

    public List<ResourceRecommendation> getRecommendations() { return recommendations; }
    public void setRecommendations(List<ResourceRecommendation> recommendations) { this.recommendations = recommendations; }

    public String getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(String generatedAt) { this.generatedAt = generatedAt; }
}

