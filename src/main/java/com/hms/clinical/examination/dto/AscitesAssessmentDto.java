package com.hms.clinical.examination.dto;

public record AscitesAssessmentDto(
        Boolean shiftingDullnessPresent,
        Boolean fluidThrillPresent,
        String notes
) {
}
