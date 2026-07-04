package com.hms.vitals.dto;

import com.hms.vitals.domain.SourceType;
import com.hms.vitals.domain.VitalParameter;

import java.time.Instant;

public record VitalAlertResponse(
        Long id,
        String patientId,
        SourceType sourceType,
        Long sourceVitalsId,
        VitalParameter parameter,
        String measuredValue,
        String message,
        boolean acknowledged,
        String acknowledgedBy,
        Instant acknowledgedAt,
        Instant createdAt
) {
}
