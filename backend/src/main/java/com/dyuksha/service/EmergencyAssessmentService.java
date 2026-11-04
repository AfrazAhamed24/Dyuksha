package com.dyuksha.service;

import com.dyuksha.dto.AssessmentResult;
import com.dyuksha.model.Emergency;

/**
 * Service representing the Emergency Assessment Agent capabilities.
 * Analyzes an incident to infer required emergency response assets.
 */
public interface EmergencyAssessmentService {

    AssessmentResult assessEmergency(Emergency emergency);
}

