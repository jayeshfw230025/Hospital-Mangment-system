package com.hms.ipd.procedure.dto;

import com.hms.clinical.complaint.SeverityLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ProcedureComplicationRequest(

        @NotNull(message = "Procedure ID is required")
        Long procedureId,

        @NotBlank(message = "Complication description is required")
        String complicationDescription,

        SeverityLevel severity,

        LocalDate reportedDate,

        String reportedByName
) {
}
