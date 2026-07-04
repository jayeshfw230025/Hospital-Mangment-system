package com.hms.ipd.admission.dto;

import jakarta.validation.constraints.NotNull;

public record BedTransferRequest(

        @NotNull(message = "Admission ID is required")
        Long admissionId,

        @NotNull(message = "New bed ID is required")
        Long newBedId,

        String reason
) {
}
