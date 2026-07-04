package com.hms.fhir.model;

import java.util.List;

public record FhirMedicationRequest(
        String resourceType,
        String id,
        String status,
        String intent,
        FhirCodeableConcept medicationCodeableConcept,
        FhirReference subject,
        String requester,
        List<FhirDosage> dosageInstruction,
        String authoredOn) {

    public FhirMedicationRequest(String id, String status, String intent, FhirCodeableConcept medicationCodeableConcept,
                                  FhirReference subject, String requester, List<FhirDosage> dosageInstruction,
                                  String authoredOn) {
        this("MedicationRequest", id, status, intent, medicationCodeableConcept, subject, requester,
                dosageInstruction, authoredOn);
    }
}
