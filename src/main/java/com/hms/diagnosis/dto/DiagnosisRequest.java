package com.hms.diagnosis.dto;

import com.hms.diagnosis.domain.DiagnosisStatus;
import com.hms.diagnosis.domain.DiagnosisType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record DiagnosisRequest(

        @NotBlank(message = "Patient ID is required")
        String patientId,

        @NotBlank(message = "ICD-10 code is required")
        String icd10Code,

        @NotNull(message = "Diagnosis type is required")
        DiagnosisType diagnosisType,

        DiagnosisStatus status,

        LocalDate diagnosedDate,

        String notes
) {
}
