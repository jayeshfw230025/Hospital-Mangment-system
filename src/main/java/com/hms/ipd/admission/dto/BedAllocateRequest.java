package com.hms.ipd.admission.dto;

import jakarta.validation.constraints.NotNull;

public record BedAllocateRequest(

        @NotNull(message = "Admission ID is required")
        Long admissionId,

        @NotNull(message = "Bed ID is required")
        Long bedId
) {
}
