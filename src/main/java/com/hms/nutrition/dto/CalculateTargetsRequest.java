package com.hms.nutrition.dto;

import com.hms.nutrition.domain.DiseaseCategory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CalculateTargetsRequest(

        @NotNull(message = "Disease category is required")
        DiseaseCategory diseaseCategory,

        @NotNull(message = "Weight (kg) is required")
        @Positive(message = "Weight must be positive")
        Double weightKg
) {
}
