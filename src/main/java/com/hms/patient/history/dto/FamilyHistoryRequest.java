package com.hms.patient.history.dto;

import jakarta.validation.constraints.NotBlank;

public record FamilyHistoryRequest(

        @NotBlank(message = "Patient ID (UPID) is required")
        String patientId,

        boolean pepticUlcerDisease,
        boolean inflammatoryBowelDisease,
        boolean giMalignancy,
        String giMalignancyType,
        boolean diabetesMellitus,
        boolean hypertension,
        boolean coronaryArteryDisease,
        String othersDescription
) {
}
