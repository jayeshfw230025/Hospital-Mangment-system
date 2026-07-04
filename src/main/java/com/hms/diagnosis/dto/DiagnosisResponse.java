package com.hms.diagnosis.dto;

import com.hms.diagnosis.domain.DiagnosisStatus;
import com.hms.diagnosis.domain.DiagnosisType;
import com.hms.diagnosis.domain.IcdCategory;

import java.time.Instant;
import java.time.LocalDate;

public record DiagnosisResponse(
        Long id,
        String patientId,
        String icd10Code,
        String icd10Description,
        IcdCategory icd10Category,
        DiagnosisType diagnosisType,
        DiagnosisStatus status,
        LocalDate diagnosedDate,
        String notes,
        Instant createdAt
) {
}
