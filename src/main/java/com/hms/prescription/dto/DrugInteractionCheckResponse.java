package com.hms.prescription.dto;

import java.util.List;

public record DrugInteractionCheckResponse(
        List<DrugInteractionWarning> interactionWarnings,
        List<NutritionAlert> nutritionAlerts
) {
}
