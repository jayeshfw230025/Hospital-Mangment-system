package com.hms.cds.dto;

import com.hms.cds.domain.CdsContext;

import java.time.Instant;

public record CdsAlertResponse(
        Long id,
        String patientId,
        CdsContext context,
        String ruleName,
        String finding,
        String suggestion,
        Instant createdAt
) {
}
