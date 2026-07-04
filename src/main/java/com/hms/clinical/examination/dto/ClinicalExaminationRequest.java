package com.hms.clinical.examination.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public record ClinicalExaminationRequest(

        @NotBlank(message = "Patient ID is required")
        String patientId,

        Long visitId,

        Long admissionId,

        @Valid
        AbdominalExaminationDto abdominalExamination,

        @Valid
        DigitalRectalExaminationDto digitalRectalExamination,

        @Valid
        JaundiceAssessmentDto jaundiceAssessment,

        @Valid
        HerniaExaminationDto herniaExamination,

        @Valid
        LymphNodeExaminationDto lymphNodeExamination,

        @Valid
        GiMassExaminationDto giMassExamination,

        @Valid
        AscitesAssessmentDto ascitesAssessment,

        @Valid
        SystemicExaminationDto systemicExamination,

        Double abdominalGirthCm
) {
}
