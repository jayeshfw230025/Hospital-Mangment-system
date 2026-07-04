package com.hms.clinical.examination.web;

import com.hms.clinical.examination.dto.ClinicalExaminationRequest;
import com.hms.clinical.examination.dto.ClinicalExaminationResponse;
import com.hms.clinical.examination.service.ClinicalExaminationService;
import com.hms.common.web.ApiResponse;
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

@Tag(name = "Clinical Examination", description = "GI-specific clinical examination capture for OPD and IPD")
@RestController
@RequestMapping("/api/v1/clinical")
public class ClinicalExaminationController {

    private final ClinicalExaminationService clinicalExaminationService;

    public ClinicalExaminationController(ClinicalExaminationService clinicalExaminationService) {
        this.clinicalExaminationService = clinicalExaminationService;
    }

    @Operation(summary = "Record a clinical examination for an OPD visit")
    @PostMapping("/opd")
    public ResponseEntity<ApiResponse<ClinicalExaminationResponse>> createOpd(@Valid @RequestBody ClinicalExaminationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("OPD clinical examination recorded successfully", clinicalExaminationService.createOpd(request)));
    }

    @Operation(summary = "Get clinical examinations recorded for an OPD visit")
    @GetMapping("/opd/{visitId}")
    public ResponseEntity<ApiResponse<List<ClinicalExaminationResponse>>> getByVisitId(@PathVariable Long visitId) {
        return ResponseEntity.ok(ApiResponse.ok(clinicalExaminationService.getByVisitId(visitId)));
    }

    @Operation(summary = "Record a clinical examination for an IPD admission")
    @PostMapping("/ipd")
    public ResponseEntity<ApiResponse<ClinicalExaminationResponse>> createIpd(@Valid @RequestBody ClinicalExaminationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("IPD clinical examination recorded successfully", clinicalExaminationService.createIpd(request)));
    }

    @Operation(summary = "Get clinical examinations recorded for an IPD admission")
    @GetMapping("/ipd/{admissionId}")
    public ResponseEntity<ApiResponse<List<ClinicalExaminationResponse>>> getByAdmissionId(@PathVariable Long admissionId) {
        return ResponseEntity.ok(ApiResponse.ok(clinicalExaminationService.getByAdmissionId(admissionId)));
    }

    @Operation(summary = "Update a clinical examination (OPD or IPD) by its own id")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClinicalExaminationResponse>> update(@PathVariable Long id,
                                                                           @Valid @RequestBody ClinicalExaminationRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Clinical examination updated successfully",
                clinicalExaminationService.update(id, request)));
    }
}
