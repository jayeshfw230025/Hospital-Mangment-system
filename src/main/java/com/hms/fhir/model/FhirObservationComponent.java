package com.hms.fhir.model;

public record FhirObservationComponent(FhirCodeableConcept code, FhirQuantity valueQuantity, String valueString) {
}
