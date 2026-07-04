package com.hms.cds.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MayoScoreRequest(

        @NotNull @Min(0) @Max(3)
        Integer stoolFrequencySubscore,

        @NotNull @Min(0) @Max(3)
        Integer rectalBleedingSubscore,

        @NotNull @Min(0) @Max(3)
        Integer endoscopySubscore,

        @NotNull @Min(0) @Max(3)
        Integer physicianGlobalAssessmentSubscore
) {
}
