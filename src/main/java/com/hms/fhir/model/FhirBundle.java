package com.hms.fhir.model;

import java.util.List;

public record FhirBundle(String resourceType, String type, int total, List<Object> entry) {

    public static FhirBundle collection(List<Object> resources) {
        return new FhirBundle("Bundle", "collection", resources.size(), resources);
    }
}
