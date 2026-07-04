package com.hms.prescription.dto;

import com.hms.prescription.domain.FoodInstruction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PrescriptionItemRequest(

        @NotNull(message = "Drug ID is required")
        Long drugId,

        @NotBlank(message = "Dosage is required")
        String dosage,

        @NotBlank(message = "Frequency is required")
        String frequency,

        String route,

        Integer durationDays,

        FoodInstruction foodInstruction,

        Integer refillsAllowed,

        Double patientWeightKg
) {
}
