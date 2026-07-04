package com.hms.ipd.mar.dto;

import com.hms.ipd.mar.domain.AdministrationStatus;

import java.time.Instant;

public record MarResponse(
        Long id,
        Long admissionId,
        Long drugId,
        String drugName,
        String dosage,
        String route,
        Instant scheduledTime,
        Instant administeredTime,
        String administeredByName,
        AdministrationStatus status,
        String notes,
        Instant createdAt
) {
}
