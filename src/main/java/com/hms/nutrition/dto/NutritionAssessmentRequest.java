package com.hms.nutrition.dto;

import com.hms.nutrition.domain.DiseaseCategory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record NutritionAssessmentRequest(

        @NotBlank(message = "Patient ID is required")
        String patientId,

        Long admissionId,

        Double weightKg,
        Double heightCm,
        Double weightLossPercent,
        Double dietaryIntakePercent,

        @Min(value = 0, message = "Disease severity score must be between 0 and 3")
        @Max(value = 3, message = "Disease severity score must be between 0 and 3")
        Integer diseaseSeverityScore,

        Boolean acuteDiseaseEffect,

        DiseaseCategory diseaseCategory,

        Double fluidRequirementMlPerDayOverride,
        Double sodiumRestrictionMeqPerDay,
        Double potassiumBalanceMeqPerDay,
        Boolean enteralParenteralSupport,
        String dieticianAssessment,
        String weeklyFollowUpPlan
) {
}
