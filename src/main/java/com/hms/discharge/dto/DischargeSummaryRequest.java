package com.hms.discharge.dto;

import com.hms.discharge.domain.DischargeCondition;
import com.hms.discharge.domain.DischargeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record DischargeSummaryRequest(

        @NotNull(message = "Admission ID is required")
        Long admissionId,

        @NotNull(message = "Discharge type is required")
        DischargeType dischargeType,

        @NotBlank(message = "Primary diagnosis (ICD-10 code) is required")
        String primaryDiagnosisIcd10,

        String secondaryDiagnosisIcd10,

        String dischargeDiagnosisText,
        String summaryOfHospitalStay,

        Instant followUpDateTime,
        String followUpInstructions,

        DischargeCondition dischargeCondition,

        @NotBlank(message = "Discharging doctor name is required")
        String dischargedByDoctorName,

        @NotBlank(message = "Discharging doctor signature is required")
        String dischargedByDoctorSignature,

        Boolean medicalRecordsChecked,
        String dischargeInstructions,

        String dischargeDietPlanOverride
) {
}
