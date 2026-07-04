package com.hms.integration.abdm.dto;

import jakarta.validation.constraints.NotBlank;

public record AbdmHealthRecordRequest(
        @NotBlank(message = "Patient UPID is required")
        String patientId,

        @NotBlank(message = "Consent ID is required")
        String consentId) {
}
