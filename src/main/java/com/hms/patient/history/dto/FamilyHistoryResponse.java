package com.hms.patient.history.dto;

import java.time.Instant;

public record FamilyHistoryResponse(
        Long id,
        String patientId,
        boolean pepticUlcerDisease,
        boolean inflammatoryBowelDisease,
        boolean giMalignancy,
        String giMalignancyType,
        boolean diabetesMellitus,
        boolean hypertension,
        boolean coronaryArteryDisease,
        String othersDescription,
        Instant createdAt
) {
}
