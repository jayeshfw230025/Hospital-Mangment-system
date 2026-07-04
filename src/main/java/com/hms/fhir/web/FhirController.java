package com.hms.fhir.web;

import com.hms.fhir.dto.DocumentReferenceRequest;
import com.hms.fhir.dto.MedicationRequestSubmission;
import com.hms.fhir.model.FhirCondition;
import com.hms.fhir.model.FhirDocumentReference;
import com.hms.fhir.model.FhirEncounter;
import com.hms.fhir.model.FhirMedicationRequest;
import com.hms.fhir.model.FhirObservation;
import com.hms.fhir.model.FhirPatient;
import com.hms.fhir.service.FhirService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "FHIR R4", description = "FHIR R4-shaped resource mapping over the internal clinical data model")
@RestController
@RequestMapping("/api/v1/fhir")
public class FhirController {

    private final FhirService fhirService;

    public FhirController(FhirService fhirService) {
        this.fhirService = fhirService;
    }

    @Operation(summary = "Fetch a patient as a FHIR Patient resource (id = UPID)")
    @GetMapping("/Patient/{id}")
    public ResponseEntity<FhirPatient> getPatient(@PathVariable String id) {
        return ResponseEntity.ok(fhirService.getPatient(id));
    }

    @Operation(summary = "Register a patient from a FHIR Patient resource")
    @PostMapping("/Patient")
    public ResponseEntity<FhirPatient> createPatient(@RequestBody FhirPatient patient) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fhirService.createPatient(patient));
    }

    @Operation(summary = "Vitals and lab results as FHIR Observation resources for a patient")
    @GetMapping("/Observation")
    public ResponseEntity<List<FhirObservation>> getObservations(
            @RequestParam String patientId,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(fhirService.getObservations(patientId, category));
    }

    @Operation(summary = "ICD-10 diagnoses as FHIR Condition resources for a patient")
    @GetMapping("/Condition")
    public ResponseEntity<List<FhirCondition>> getConditions(@RequestParam String patientId) {
        return ResponseEntity.ok(fhirService.getConditions(patientId));
    }

    @Operation(summary = "Create a prescription from a FHIR-shaped MedicationRequest submission")
    @PostMapping("/MedicationRequest")
    public ResponseEntity<FhirMedicationRequest> createMedicationRequest(
            @Valid @RequestBody MedicationRequestSubmission submission) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fhirService.createMedicationRequest(submission));
    }

    @Operation(summary = "OPD visits and IPD admissions as FHIR Encounter resources for a patient")
    @GetMapping("/Encounter")
    public ResponseEntity<List<FhirEncounter>> getEncounters(@RequestParam String patientId) {
        return ResponseEntity.ok(fhirService.getEncounters(patientId));
    }

    @Operation(summary = "Build a FHIR DocumentReference for a lab report, consent form or discharge summary")
    @PostMapping("/DocumentReference")
    public ResponseEntity<FhirDocumentReference> createDocumentReference(
            @Valid @RequestBody DocumentReferenceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fhirService.createDocumentReference(request));
    }
}
