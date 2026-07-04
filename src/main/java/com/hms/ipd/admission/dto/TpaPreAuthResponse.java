package com.hms.ipd.admission.dto;

import com.hms.ipd.admission.domain.PreAuthStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record TpaPreAuthResponse(
        Long id,
        Long admissionId,
        String insuranceCompanyName,
        String policyNumber,
        String preAuthNumber,
        LocalDate preAuthDate,
        PreAuthStatus approvalStatus,
        BigDecimal estimatedCost,
        Instant createdAt
) {
}
