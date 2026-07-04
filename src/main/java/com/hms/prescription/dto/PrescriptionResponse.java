package com.hms.prescription.dto;

import com.hms.prescription.domain.GastroTemplateType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record PrescriptionResponse(
        Long id,
        String patientId,
        Long visitId,
        Long admissionId,
        LocalDate prescribedDate,
        String doctorName,
        GastroTemplateType templateUsed,
        List<PrescriptionItemResponse> items,
        List<DrugInteractionWarning> interactionWarnings,
        List<NutritionAlert> nutritionAlerts,
        Instant createdAt
) {
}
