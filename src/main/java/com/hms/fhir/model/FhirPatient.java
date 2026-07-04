package com.hms.fhir.model;

import java.util.List;

public record FhirPatient(
        String resourceType,
        String id,
        List<FhirIdentifier> identifier,
        List<FhirHumanName> name,
        String gender,
        String birthDate,
        List<FhirContactPoint> telecom) {

    public FhirPatient(String id, List<FhirIdentifier> identifier, List<FhirHumanName> name,
                        String gender, String birthDate, List<FhirContactPoint> telecom) {
        this("Patient", id, identifier, name, gender, birthDate, telecom);
    }
}
