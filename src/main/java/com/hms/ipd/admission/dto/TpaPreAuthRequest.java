package com.hms.ipd.admission.dto;

import com.hms.ipd.admission.domain.PreAuthStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TpaPreAuthRequest(

        @NotNull(message = "Admission ID is required")
        Long admissionId,

        @NotBlank(message = "Insurance company name is required")
        String insuranceCompanyName,

        @NotBlank(message = "Policy number is required")
        String policyNumber,

        String preAuthNumber,

        LocalDate preAuthDate,

        PreAuthStatus approvalStatus,

        BigDecimal estimatedCost
) {
}
