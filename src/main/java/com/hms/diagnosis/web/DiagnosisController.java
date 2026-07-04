package com.hms.diagnosis.web;

import com.hms.common.web.ApiResponse;
import com.hms.diagnosis.dto.DiagnosisRequest;
import com.hms.diagnosis.dto.DiagnosisResponse;
import com.hms.diagnosis.service.DiagnosisService;
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

import java.util.List;

@Tag(name = "Diagnosis", description = "ICD-10 linked patient diagnosis records")
@RestController
@RequestMapping("/api/v1/diagnosis")
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    public DiagnosisController(DiagnosisService diagnosisService) {
        this.diagnosisService = diagnosisService;
    }

    @Operation(summary = "Record a diagnosis for a patient")
    @PostMapping
    public ResponseEntity<ApiResponse<DiagnosisResponse>> create(@Valid @RequestBody DiagnosisRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Diagnosis recorded successfully", diagnosisService.create(request)));
    }

    @Operation(summary = "Get all diagnoses recorded for a patient")
    @GetMapping("/{patientId}")
    public ResponseEntity<ApiResponse<List<DiagnosisResponse>>> getByPatientId(@PathVariable String patientId) {
        return ResponseEntity.ok(ApiResponse.ok(diagnosisService.getByPatientId(patientId)));
    }

    @Operation(summary = "Update a diagnosis by its own id")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DiagnosisResponse>> update(@PathVariable Long id,
                                                                  @Valid @RequestBody DiagnosisRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Diagnosis updated successfully", diagnosisService.update(id, request)));
    }
}
