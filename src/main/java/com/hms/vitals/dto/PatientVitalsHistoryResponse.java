package com.hms.vitals.dto;

import java.util.List;

public record PatientVitalsHistoryResponse(
        String patientId,
        List<OpdVitalsResponse> opdVitals,
        List<IpdVitalsResponse> ipdVitals
) {
}
