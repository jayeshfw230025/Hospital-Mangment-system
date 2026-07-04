package com.hms.cds.dto;

import com.hms.cds.score.AscitesGrade;
import com.hms.cds.score.EncephalopathyGrade;
import jakarta.validation.constraints.NotNull;

public record CtpScoreRequest(

        @NotNull(message = "Ascites grade is required")
        AscitesGrade ascites,

        @NotNull(message = "Encephalopathy grade is required")
        EncephalopathyGrade encephalopathy,

        @NotNull(message = "Bilirubin is required")
        Double bilirubinMgDl,

        @NotNull(message = "Albumin is required")
        Double albuminGDl,

        @NotNull(message = "INR is required")
        Double inr
) {
}
