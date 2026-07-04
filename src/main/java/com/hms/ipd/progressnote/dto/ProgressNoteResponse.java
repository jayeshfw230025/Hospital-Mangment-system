package com.hms.ipd.progressnote.dto;

import com.hms.clinical.complaint.SeverityLevel;
import com.hms.ipd.progressnote.domain.ActivityLevel;
import com.hms.ipd.progressnote.domain.AppetiteLevel;
import com.hms.vitals.dto.IpdVitalsResponse;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record ProgressNoteResponse(
        Long id,
        Long admissionId,
        LocalDate noteDate,

        String chiefComplaintToday,
        Integer painScore,
        Boolean nauseaVomiting,
        AppetiteLevel appetite,
        String bowelMovementFrequency,
        String bowelMovementCharacter,
        String sleepPattern,
        String generalWellBeing,

        IpdVitalsResponse vitalsSnapshot,
        String generalAppearance,
        String abdominalExaminationFindings,
        String newFindings,

        String clinicalImpression,
        String currentDiagnosis,
        String icd10Code,
        String icd10Description,
        SeverityLevel severityAssessment,
        List<String> complicationFlags,

        List<MedicationPlanItemDto> medicationPlanItems,
        List<String> investigationsOrdered,
        List<String> consultationsRequired,
        String dietPlan,
        ActivityLevel activityLevel,
        String dischargePlanningNotes,

        Instant createdAt
) {
}
