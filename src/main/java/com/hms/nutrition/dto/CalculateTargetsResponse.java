package com.hms.nutrition.dto;

import com.hms.nutrition.domain.DiseaseCategory;

public record CalculateTargetsResponse(
        DiseaseCategory diseaseCategory,
        double caloricTargetMinKcalPerDay,
        double caloricTargetMaxKcalPerDay,
        double proteinTargetMinGPerDay,
        double proteinTargetMaxGPerDay,
        double fluidRequirementMlPerDay
) {
}
