package com.hms.ipd.admission.dto;

import com.hms.ipd.admission.domain.AdmissionSource;
import com.hms.ipd.admission.domain.AdmissionType;
import com.hms.patient.history.dto.CurrentMedicationDto;

import java.time.Instant;
import java.util.List;

public record IpdAdmissionResponse(
        Long id,
        String patientId,
        Instant admissionDateTime,
        AdmissionType admissionType,
        AdmissionSource admissionSource,
        String referralDoctorName,
        String referralDoctorContact,
        String referringHospitalName,
        String referringHospitalContact,
        String primaryDiagnosisIcd10,
        String primaryDiagnosisDescription,
        String secondaryDiagnosisIcd10,
        String secondaryDiagnosisDescription,
        String clinicalSummary,
        List<String> hardStopAllergies,
        List<CurrentMedicationDto> currentMedications,
        boolean hasConsentDocument,
        BedResponse bed,
        TpaPreAuthResponse latestTpaPreAuth,
        Instant createdAt
) {
}
