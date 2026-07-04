package com.hms.investigation.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record InvestigationReportResponse(
        Long id,
        Long orderId,
        String patientId,
        String investigationTypeCode,
        String investigationName,
        LocalDate reportDate,
        List<ResultParameterResponse> resultParameters,
        boolean hasFile,
        String reportFileName,
        String notes,
        Instant createdAt
) {
}
