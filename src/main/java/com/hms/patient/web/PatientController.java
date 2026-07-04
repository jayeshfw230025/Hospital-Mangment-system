package com.hms.patient.web;

import com.hms.common.web.ApiResponse;
import com.hms.patient.dto.AbhaLinkInitiationResponse;
import com.hms.patient.dto.OtpVerificationRequest;
import com.hms.patient.dto.PatientRegistrationRequest;
import com.hms.patient.dto.PatientResponse;
import com.hms.patient.dto.PatientUpdateRequest;
import com.hms.patient.dto.ReferralDetailsDto;
import com.hms.patient.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "Patient Registration", description = "Patient registration and demographics")
@RestController
@RequestMapping("/api/v1/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @Operation(summary = "Register a new patient")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<PatientResponse>> register(@Valid @RequestBody PatientRegistrationRequest request) {
        PatientResponse response = patientService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Patient registered successfully", response));
    }

    @Operation(summary = "Get patient by UPID")
    @GetMapping("/{upid}")
    public ResponseEntity<ApiResponse<PatientResponse>> getByUpid(@PathVariable String upid) {
        return ResponseEntity.ok(ApiResponse.ok(patientService.getByUpid(upid)));
    }

    @Operation(summary = "Update patient demographics by UPID")
    @PutMapping("/{upid}")
    public ResponseEntity<ApiResponse<PatientResponse>> update(@PathVariable String upid,
                                                                @Valid @RequestBody PatientUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Patient updated successfully", patientService.update(upid, request)));
    }

    @Operation(summary = "Search patients by name, contact number, date of birth and/or UPID")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<PatientResponse>>> search(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String contactNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,
            @RequestParam(required = false) String upid,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                patientService.search(fullName, contactNumber, dateOfBirth, upid, pageable)));
    }

    @Operation(summary = "Get patient QR code (base64 PNG)")
    @GetMapping("/{upid}/qr-code")
    public ResponseEntity<ApiResponse<String>> getQrCode(@PathVariable String upid) {
        return ResponseEntity.ok(ApiResponse.ok(patientService.getQrCode(upid)));
    }

    @Operation(summary = "Initiate ABHA linkage for a patient; sends an OTP to complete verification")
    @GetMapping("/link-abha/{upid}")
    public ResponseEntity<ApiResponse<AbhaLinkInitiationResponse>> linkAbha(@PathVariable String upid,
                                                                             @RequestParam String abhaNumber) {
        return ResponseEntity.ok(ApiResponse.ok(patientService.initiateAbhaLink(upid, abhaNumber)));
    }

    @Operation(summary = "Verify OTP to complete ABHA linkage")
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<PatientResponse>> verifyOtp(@Valid @RequestBody OtpVerificationRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("ABHA linked successfully",
                patientService.verifyOtpAndCompleteAbhaLink(request.txnId(), request.otp())));
    }

    @Operation(summary = "Get referral details for a patient by UPID")
    @GetMapping("/referrals/{upid}")
    public ResponseEntity<ApiResponse<ReferralDetailsDto>> getReferral(@PathVariable String upid) {
        return ResponseEntity.ok(ApiResponse.ok(patientService.getReferral(upid)));
    }
}
