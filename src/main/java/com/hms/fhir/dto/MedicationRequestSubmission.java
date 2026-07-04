package com.hms.fhir.dto;

import com.hms.prescription.domain.FoodInstruction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Minimal FHIR-adjacent submission for POST /api/v1/fhir/MedicationRequest.
 * A bare FHIR MedicationRequest identifies the drug by a coded/text
 * medicationCodeableConcept, which cannot be reliably resolved to one of our
 * internal Drug master rows by free text alone - so the caller supplies the
 * internal drugId directly (as if it were an "internal-drug-id" identifier
 * alongside the FHIR coding), and this is mapped straight into the existing,
 * already-tested prescription creation pipeline (allergy hard-stop / interaction
 * checks included) rather than re-implementing that logic here.
 */
public record MedicationRequestSubmission(
        @NotBlank(message = "Patient UPID (subject) is required")
        String patientId,

        Long visitId,

        Long admissionId,

        @NotBlank(message = "Requester (doctor name) is required")
        String requester,

        @NotBlank(message = "Digital signature is required")
        String digitalSignature,

        @NotNull(message = "Internal drug ID is required")
        Long drugId,

        @NotBlank(message = "Dosage is required")
        String dosage,

        @NotBlank(message = "Frequency is required")
        String frequency,

        String route,

        Integer durationDays,

        FoodInstruction foodInstruction
) {
}
