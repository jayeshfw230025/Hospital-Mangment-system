package com.hms.cds.dto;

import com.hms.cds.domain.AdditionalFindings;
import jakarta.validation.constraints.NotBlank;

public record CdsAssessRequest(

        @NotBlank(message = "Patient ID is required")
        String patientId,

        Long visitId,

        Long admissionId,

        AdditionalFindings additionalFindings
) {
}
