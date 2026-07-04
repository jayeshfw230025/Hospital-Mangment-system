package com.hms.fhir.model;

import java.util.List;

public record FhirCodeableConcept(List<FhirCoding> coding, String text) {

    public static FhirCodeableConcept ofText(String text) {
        return new FhirCodeableConcept(List.of(), text);
    }

    public static FhirCodeableConcept ofCode(String system, String code, String display) {
        return new FhirCodeableConcept(List.of(new FhirCoding(system, code, display)), display);
    }
}
