package com.hms.investigation.dto;

import com.hms.investigation.domain.InvestigationCategory;
import com.hms.investigation.domain.OrderStatus;

import java.time.Instant;
import java.time.LocalDate;

public record InvestigationOrderResponse(
        Long id,
        String patientId,
        Long visitId,
        Long admissionId,
        String investigationTypeCode,
        String investigationName,
        InvestigationCategory category,
        LocalDate orderedDate,
        OrderStatus status,
        String notes,
        InvestigationReportResponse latestReport,
        Instant createdAt
) {
}
