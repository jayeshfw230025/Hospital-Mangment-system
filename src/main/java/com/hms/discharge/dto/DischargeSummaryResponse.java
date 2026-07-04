package com.hms.discharge.dto;

import com.hms.discharge.domain.DischargeCondition;
import com.hms.discharge.domain.DischargeType;
import com.hms.patient.domain.Gender;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record DischargeSummaryResponse(
        Long id,
        Long admissionId,

        String patientId,
        String patientName,
        LocalDate patientDateOfBirth,
        Gender patientGender,

        Instant admissionDateTime,
        Instant dischargeDateTime,
        Integer lengthOfStayDays,

        DischargeType dischargeType,
        String primaryDiagnosisIcd10,
        String primaryDiagnosisDescription,
        String secondaryDiagnosisIcd10,
        String secondaryDiagnosisDescription,
        String dischargeDiagnosisText,
        String summaryOfHospitalStay,

        List<String> significantProcedures,
        List<String> complicationsDuringStay,
        List<DischargeMedicationItemDto> dischargeMedications,
        String dischargeDietPlan,

        Instant followUpDateTime,
        String followUpInstructions,

        DischargeCondition dischargeCondition,
        String dischargedByDoctorName,
        Boolean medicalRecordsChecked,
        String dischargeInstructions,

        Instant createdAt
) {
}
