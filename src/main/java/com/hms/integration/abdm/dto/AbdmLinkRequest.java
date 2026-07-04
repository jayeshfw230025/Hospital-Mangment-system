package com.hms.integration.abdm.dto;

import jakarta.validation.constraints.NotBlank;

public record AbdmLinkRequest(
        @NotBlank(message = "Patient UPID is required")
        String patientId,

        @NotBlank(message = "ABHA number is required")
        String abhaNumber) {
}
