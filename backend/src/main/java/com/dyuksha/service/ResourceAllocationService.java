package com.dyuksha.service;

import com.dyuksha.dto.AssessmentResult;
import com.dyuksha.dto.ResourceRecommendation;
import com.dyuksha.model.Emergency;
import com.dyuksha.model.Resource;

import java.util.List;

/**
 * Service representing the Resource Allocation Agent.
 * Evaluates candidate resources against incident profiles using multi-criteria weighted scoring.
 */
public interface ResourceAllocationService {

    double calculateDistance(double lat1, double lon1, double lat2, double lon2);

    List<ResourceRecommendation> rankResources(Emergency emergency, AssessmentResult assessment, List<Resource> availableResources);
}

