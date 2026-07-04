package com.hms.ipd.admission.dto;

import com.hms.ipd.admission.domain.AdmissionSource;
import com.hms.ipd.admission.domain.AdmissionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record IpdAdmissionRequest(

        @NotBlank(message = "Patient ID is required")
        String patientId,

        @NotNull(message = "Admission type is required")
        AdmissionType admissionType,

        @NotNull(message = "Admission source is required")
        AdmissionSource admissionSource,

        String referralDoctorName,
        String referralDoctorContact,
        String referringHospitalName,
        String referringHospitalContact,

        @NotBlank(message = "Primary diagnosis (ICD-10 code) is required")
        String primaryDiagnosisIcd10,

        String secondaryDiagnosisIcd10,

        String clinicalSummary,

        @NotBlank(message = "Consent signature is required")
        String consentSignature
) {
}
