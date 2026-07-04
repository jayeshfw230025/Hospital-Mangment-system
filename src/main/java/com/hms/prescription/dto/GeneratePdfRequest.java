package com.hms.prescription.dto;

import jakarta.validation.constraints.NotNull;

public record GeneratePdfRequest(

        @NotNull(message = "Prescription ID is required")
        Long prescriptionId
) {
}
