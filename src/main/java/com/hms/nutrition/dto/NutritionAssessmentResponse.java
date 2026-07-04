package com.hms.nutrition.dto;

import com.hms.nutrition.domain.DiseaseCategory;
import com.hms.nutrition.domain.MustRiskCategory;

import java.time.Instant;
import java.time.LocalDate;

public record NutritionAssessmentResponse(
        Long id,
        String patientId,
        Long admissionId,
        LocalDate assessmentDate,

        Double weightKg,
        Double heightCm,
        Double bmi,
        Integer age,

        Double weightLossPercent,
        Double dietaryIntakePercent,
        Integer diseaseSeverityScore,
        Integer nrsNutritionalStatusScore,
        Integer nrsAgeAdjustment,
        Integer nrsTotalScore,
        Boolean nrsAtRisk,

        Boolean acuteDiseaseEffect,
        Integer mustBmiScore,
        Integer mustWeightLossScore,
        Integer mustAcuteDiseaseScore,
        Integer mustTotalScore,
        MustRiskCategory mustRiskCategory,

        DiseaseCategory diseaseCategory,
        Double caloricTargetMinKcalPerDay,
        Double caloricTargetMaxKcalPerDay,
        Double proteinTargetMinGPerDay,
        Double proteinTargetMaxGPerDay,
        Double fluidRequirementMlPerDay,
        Double sodiumRestrictionMeqPerDay,
        Double potassiumBalanceMeqPerDay,
        Boolean enteralParenteralSupport,
        String dieticianAssessment,
        String weeklyFollowUpPlan,

        Instant createdAt
) {
}
