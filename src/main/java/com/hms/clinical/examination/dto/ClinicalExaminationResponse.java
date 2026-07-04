package com.hms.clinical.examination.dto;

import com.hms.clinical.examination.ExaminationContext;

import java.time.Instant;

public record ClinicalExaminationResponse(
        Long id,
        ExaminationContext examinationContext,
        Long visitId,
        Long admissionId,
        String patientId,
        AbdominalExaminationDto abdominalExamination,
        DigitalRectalExaminationDto digitalRectalExamination,
        JaundiceAssessmentDto jaundiceAssessment,
        HerniaExaminationDto herniaExamination,
        LymphNodeExaminationDto lymphNodeExamination,
        GiMassExaminationDto giMassExamination,
        AscitesAssessmentDto ascitesAssessment,
        SystemicExaminationDto systemicExamination,
        Double abdominalGirthCm,
        Instant createdAt
) {
}
