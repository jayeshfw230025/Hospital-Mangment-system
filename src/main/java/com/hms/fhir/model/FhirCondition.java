package com.hms.fhir.model;

public record FhirCondition(
        String resourceType,
        String id,
        FhirCodeableConcept clinicalStatus,
        FhirCodeableConcept code,
        FhirReference subject,
        String onsetDateTime,
        String note) {

    public FhirCondition(String id, FhirCodeableConcept clinicalStatus, FhirCodeableConcept code,
                          FhirReference subject, String onsetDateTime, String note) {
        this("Condition", id, clinicalStatus, code, subject, onsetDateTime, note);
    }
}
