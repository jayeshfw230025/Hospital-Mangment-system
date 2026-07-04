package com.hms.clinical.examination.dto;

public record JaundiceAssessmentDto(
        Boolean icterusSclera,
        Boolean icterusSkin,
        Boolean icterusPalmar,
        Boolean scratchMarksPresent
) {
}
