package com.hms.ipd.procedure.dto;

import com.hms.ipd.procedure.domain.ProcedureType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Map;

public record ProcedureRequest(

        @NotNull(message = "Admission ID is required")
        Long admissionId,

        @NotNull(message = "Procedure type is required")
        ProcedureType procedureType,

        LocalDate procedureDate,

        String performedByName,

        String notes,

        Map<String, Object> details
) {
}
