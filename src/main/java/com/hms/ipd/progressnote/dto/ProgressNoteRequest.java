package com.hms.ipd.progressnote.dto;

import com.hms.clinical.complaint.SeverityLevel;
import com.hms.ipd.progressnote.domain.ActivityLevel;
import com.hms.ipd.progressnote.domain.AppetiteLevel;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ProgressNoteRequest(

        @NotNull(message = "Admission ID is required")
        Long admissionId,

        String chiefComplaintToday,

        @Min(0) @Max(10)
        Integer painScore,

        Boolean nauseaVomiting,
        AppetiteLevel appetite,
        String bowelMovementFrequency,
        String bowelMovementCharacter,
        String sleepPattern,
        String generalWellBeing,

        String generalAppearance,
        String abdominalExaminationFindings,
        String newFindings,

        String clinicalImpression,
        String currentDiagnosis,
        String icd10Code,
        SeverityLevel severityAssessment,
        List<String> complicationFlags,

        List<MedicationPlanItemDto> medicationPlanItems,
        List<String> investigationsOrdered,
        List<String> consultationsRequired,
        String dietPlan,
        ActivityLevel activityLevel,
        String dischargePlanningNotes
) {
}
