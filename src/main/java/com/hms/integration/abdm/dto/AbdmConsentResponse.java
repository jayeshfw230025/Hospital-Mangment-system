package com.hms.integration.abdm.dto;

import com.hms.integration.abdm.domain.ConsentStatus;

import java.time.Instant;
import java.util.List;

public record AbdmConsentResponse(
        String consentId,
        String patientId,
        String purpose,
        List<String> hiTypes,
        ConsentStatus status,
        Instant grantedAt,
        Instant expiresAt) {
}
