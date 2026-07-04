package com.hms.patient.history.web;

import com.hms.common.web.ApiResponse;
import com.hms.patient.history.dto.LifestyleResponse;
import com.hms.patient.history.dto.PatientHistoryRequest;
import com.hms.patient.history.dto.PatientHistoryResponse;
import com.hms.patient.history.service.PatientHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Patient History", description = "Medical history and lifestyle capture")
@RestController
@RequestMapping("/api/v1/patients")
public class PatientHistoryController {

    private final PatientHistoryService patientHistoryService;

    public PatientHistoryController(PatientHistoryService patientHistoryService) {
        this.patientHistoryService = patientHistoryService;
    }

    @Operation(summary = "Record medical history (and lifestyle) for a patient")
    @PostMapping("/history")
    public ResponseEntity<ApiResponse<PatientHistoryResponse>> create(@Valid @RequestBody PatientHistoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Patient history recorded successfully", patientHistoryService.create(request)));
    }

    @Operation(summary = "Get medical history (and lifestyle) for a patient by UPID")
    @GetMapping("/history/{patientId}")
    public ResponseEntity<ApiResponse<PatientHistoryResponse>> getByPatientId(@PathVariable String patientId) {
        return ResponseEntity.ok(ApiResponse.ok(patientHistoryService.getByPatientId(patientId)));
    }

    @Operation(summary = "Update medical history (and lifestyle) by its own id")
    @PutMapping("/history/{id}")
    public ResponseEntity<ApiResponse<PatientHistoryResponse>> update(@PathVariable Long id,
                                                                       @Valid @RequestBody PatientHistoryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Patient history updated successfully",
                patientHistoryService.update(id, request)));
    }

    @Operation(summary = "Get the lifestyle subset of a patient's history")
    @GetMapping("/lifestyle/{patientId}")
    public ResponseEntity<ApiResponse<LifestyleResponse>> getLifestyle(@PathVariable String patientId) {
        return ResponseEntity.ok(ApiResponse.ok(patientHistoryService.getLifestyle(patientId)));
    }
}
