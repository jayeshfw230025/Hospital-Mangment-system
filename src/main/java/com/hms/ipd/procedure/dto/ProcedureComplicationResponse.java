package com.hms.ipd.procedure.dto;

import com.hms.clinical.complaint.SeverityLevel;

import java.time.Instant;
import java.time.LocalDate;

public record ProcedureComplicationResponse(
        Long id,
        Long procedureId,
        String complicationDescription,
        SeverityLevel severity,
        LocalDate reportedDate,
        String reportedByName,
        Instant createdAt
) {
}
