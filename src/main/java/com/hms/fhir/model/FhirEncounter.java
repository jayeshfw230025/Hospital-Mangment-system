package com.hms.fhir.model;

import java.util.List;

public record FhirEncounter(
        String resourceType,
        String id,
        String status,
        FhirCoding classCoding,
        FhirReference subject,
        FhirPeriod period,
        List<FhirCodeableConcept> reasonCode) {

    public FhirEncounter(String id, String status, FhirCoding classCoding, FhirReference subject,
                          FhirPeriod period, List<FhirCodeableConcept> reasonCode) {
        this("Encounter", id, status, classCoding, subject, period, reasonCode);
    }
}
