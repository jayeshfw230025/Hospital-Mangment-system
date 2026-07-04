package com.hms.vitals.web;

import com.hms.common.web.ApiResponse;
import com.hms.vitals.dto.AcknowledgeAlertRequest;
import com.hms.vitals.dto.IpdVitalsRequest;
import com.hms.vitals.dto.IpdVitalsResponse;
import com.hms.vitals.dto.OpdVitalsRequest;
import com.hms.vitals.dto.OpdVitalsResponse;
import com.hms.vitals.dto.PatientVitalsHistoryResponse;
import com.hms.vitals.dto.VitalAlertResponse;
import com.hms.vitals.service.IpdVitalsService;
import com.hms.vitals.service.OpdVitalsService;
import com.hms.vitals.service.VitalAlertService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Vitals", description = "OPD/IPD vitals capture with auto-alert engine")
@RestController
@RequestMapping("/api/v1/vitals")
public class VitalsController {

    private final OpdVitalsService opdVitalsService;
    private final IpdVitalsService ipdVitalsService;
    private final VitalAlertService vitalAlertService;

    public VitalsController(OpdVitalsService opdVitalsService,
                             IpdVitalsService ipdVitalsService,
                             VitalAlertService vitalAlertService) {
        this.opdVitalsService = opdVitalsService;
        this.ipdVitalsService = ipdVitalsService;
        this.vitalAlertService = vitalAlertService;
    }

    @Operation(summary = "Record OPD vitals (10 parameters); auto-computes BMI and evaluates critical alerts")
    @PostMapping("/opd")
    public ResponseEntity<ApiResponse<OpdVitalsResponse>> recordOpd(@Valid @RequestBody OpdVitalsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("OPD vitals recorded successfully", opdVitalsService.record(request)));
    }

    @Operation(summary = "Record IPD vitals (16 parameters); auto-computes BMI/MAP and evaluates critical alerts")
    @PostMapping("/ipd")
    public ResponseEntity<ApiResponse<IpdVitalsResponse>> recordIpd(@Valid @RequestBody IpdVitalsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("IPD vitals recorded successfully", ipdVitalsService.record(request)));
    }

    @Operation(summary = "Get all OPD and IPD vitals recorded for a patient")
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<PatientVitalsHistoryResponse>> getByPatient(@PathVariable String patientId) {
        PatientVitalsHistoryResponse response = new PatientVitalsHistoryResponse(
                patientId,
                opdVitalsService.getByPatientId(patientId),
                ipdVitalsService.getByPatientId(patientId));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Get OPD vitals recorded for a specific visit")
    @GetMapping("/visit/{visitId}")
    public ResponseEntity<ApiResponse<List<OpdVitalsResponse>>> getByVisit(@PathVariable Long visitId) {
        return ResponseEntity.ok(ApiResponse.ok(opdVitalsService.getByVisitId(visitId)));
    }

    @Operation(summary = "Get all vital alerts raised for a patient")
    @GetMapping("/alerts/{patientId}")
    public ResponseEntity<ApiResponse<List<VitalAlertResponse>>> getAlerts(@PathVariable String patientId) {
        return ResponseEntity.ok(ApiResponse.ok(vitalAlertService.getByPatientId(patientId)));
    }

    @Operation(summary = "Acknowledge a vital alert")
    @PostMapping("/acknowledge-alert")
    public ResponseEntity<ApiResponse<VitalAlertResponse>> acknowledgeAlert(@Valid @RequestBody AcknowledgeAlertRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Alert acknowledged successfully",
                vitalAlertService.acknowledge(request.alertId(), request.acknowledgedBy())));
    }
}
