package com.hms.prescription.dto;

import com.hms.prescription.domain.GastroTemplateType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PrescriptionRequest(

        @NotBlank(message = "Patient ID is required")
        String patientId,

        Long visitId,

        Long admissionId,

        @NotBlank(message = "Doctor name is required")
        String doctorName,

        @NotBlank(message = "Digital signature is required")
        String digitalSignature,

        GastroTemplateType templateUsed,

        @NotEmpty(message = "At least one prescription item is required")
        @Valid
        List<PrescriptionItemRequest> items
) {
}
