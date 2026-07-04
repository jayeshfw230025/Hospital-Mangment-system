package com.hms.fhir.model;

import java.util.List;

public record FhirDocumentReference(
        String resourceType,
        String id,
        String status,
        FhirCodeableConcept type,
        FhirReference subject,
        String date,
        String description,
        List<FhirDocumentContent> content) {

    public FhirDocumentReference(String id, String status, FhirCodeableConcept type, FhirReference subject,
                                  String date, String description, List<FhirDocumentContent> content) {
        this("DocumentReference", id, status, type, subject, date, description, content);
    }
}
