package com.hms.fhir.model;

import java.util.List;

public record FhirObservation(
        String resourceType,
        String id,
        String status,
        FhirCodeableConcept category,
        FhirCodeableConcept code,
        FhirReference subject,
        String effectiveDateTime,
        List<FhirObservationComponent> component) {

    public FhirObservation(String id, String status, FhirCodeableConcept category, FhirCodeableConcept code,
                            FhirReference subject, String effectiveDateTime, List<FhirObservationComponent> component) {
        this("Observation", id, status, category, code, subject, effectiveDateTime, component);
    }
}
