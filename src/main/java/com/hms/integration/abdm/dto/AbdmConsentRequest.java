package com.hms.integration.abdm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AbdmConsentRequest(
        @NotBlank(message = "Patient UPID is required")
        String patientId,

        @NotBlank(message = "Purpose is required")
        String purpose,

        @NotEmpty(message = "At least one health information type is required")
        List<String> hiTypes,

        Integer validityDays) {
}
