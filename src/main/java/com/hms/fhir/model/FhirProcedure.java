package com.hms.fhir.model;

import java.util.List;

public record FhirProcedure(
        String resourceType,
        String id,
        String status,
        FhirCodeableConcept code,
        FhirReference subject,
        String performedDateTime,
        List<FhirCodeableConcept> complication,
        String note) {

    public FhirProcedure(String id, String status, FhirCodeableConcept code, FhirReference subject,
                          String performedDateTime, List<FhirCodeableConcept> complication, String note) {
        this("Procedure", id, status, code, subject, performedDateTime, complication, note);
    }
}
