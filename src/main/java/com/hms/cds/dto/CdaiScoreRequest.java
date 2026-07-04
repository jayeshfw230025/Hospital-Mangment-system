package com.hms.cds.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CdaiScoreRequest(

        @NotNull @Min(0)
        Integer stoolFrequencySum,

        @NotNull @Min(0)
        Integer abdominalPainSum,

        @NotNull @Min(0)
        Integer wellBeingSum,

        @NotNull @Min(0)
        Integer extraintestinalManifestationsCount
) {
}
