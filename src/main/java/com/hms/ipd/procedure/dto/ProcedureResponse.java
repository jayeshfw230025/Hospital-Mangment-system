package com.hms.ipd.procedure.dto;

import com.hms.ipd.procedure.domain.ProcedureType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record ProcedureResponse(
        Long id,
        Long admissionId,
        ProcedureType procedureType,
        String procedureTypeLabel,
        LocalDate procedureDate,
        String performedByName,
        String notes,
        Map<String, Object> details,
        List<ProcedureComplicationResponse> complications,
        Instant createdAt
) {
}
