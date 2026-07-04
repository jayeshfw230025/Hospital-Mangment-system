package com.hms.fhir.model;

public record FhirReference(String reference, String display) {

    public static FhirReference toPatient(String upid, String displayName) {
        return new FhirReference("Patient/" + upid, displayName);
    }
}
