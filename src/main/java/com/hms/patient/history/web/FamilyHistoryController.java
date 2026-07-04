package com.hms.patient.history.web;

import com.hms.common.web.ApiResponse;
import com.hms.patient.history.dto.FamilyHistoryRequest;
import com.hms.patient.history.dto.FamilyHistoryResponse;
import com.hms.patient.history.service.FamilyHistoryService;
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

@Tag(name = "Family History", description = "GI-focused family history capture")
@RestController
@RequestMapping("/api/v1/patients/family-history")
public class FamilyHistoryController {

    private final FamilyHistoryService familyHistoryService;

    public FamilyHistoryController(FamilyHistoryService familyHistoryService) {
        this.familyHistoryService = familyHistoryService;
    }

    @Operation(summary = "Record family history for a patient")
    @PostMapping
    public ResponseEntity<ApiResponse<FamilyHistoryResponse>> create(@Valid @RequestBody FamilyHistoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Family history recorded successfully", familyHistoryService.create(request)));
    }

    @Operation(summary = "Get family history for a patient by UPID")
    @GetMapping("/{patientId}")
    public ResponseEntity<ApiResponse<FamilyHistoryResponse>> getByPatientId(@PathVariable String patientId) {
        return ResponseEntity.ok(ApiResponse.ok(familyHistoryService.getByPatientId(patientId)));
    }

    @Operation(summary = "Update family history by its own id")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FamilyHistoryResponse>> update(@PathVariable Long id,
                                                                      @Valid @RequestBody FamilyHistoryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Family history updated successfully",
                familyHistoryService.update(id, request)));
    }
}
