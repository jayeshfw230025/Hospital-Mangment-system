package com.hms.investigation.dto;

import jakarta.validation.constraints.NotBlank;

public record InvestigationOrderRequest(

        @NotBlank(message = "Patient ID is required")
        String patientId,

        Long visitId,

        Long admissionId,

        @NotBlank(message = "Investigation type code is required")
        String investigationTypeCode,

        String notes
) {
}
