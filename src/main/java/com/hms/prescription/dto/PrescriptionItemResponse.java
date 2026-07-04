package com.hms.prescription.dto;

import com.hms.prescription.domain.FoodInstruction;

public record PrescriptionItemResponse(
        Long drugId,
        String genericName,
        String brandName,
        String dosage,
        String frequency,
        String route,
        Integer durationDays,
        FoodInstruction foodInstruction,
        String generatedInstructions,
        Integer refillsAllowed,
        Integer refillsUsed,
        Double calculatedPediatricDoseMg
) {
}
