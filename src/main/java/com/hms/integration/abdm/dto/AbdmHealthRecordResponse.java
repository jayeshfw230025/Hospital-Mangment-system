package com.hms.integration.abdm.dto;

import com.hms.fhir.model.FhirBundle;

public record AbdmHealthRecordResponse(String patientId, String consentId, String message, FhirBundle bundle) {
}
