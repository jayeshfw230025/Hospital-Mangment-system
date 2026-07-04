package com.hms.integration.abdm.web;

import com.hms.common.web.ApiResponse;
import com.hms.integration.abdm.dto.AbdmConsentRequest;
import com.hms.integration.abdm.dto.AbdmConsentResponse;
import com.hms.integration.abdm.dto.AbdmHealthRecordRequest;
import com.hms.integration.abdm.dto.AbdmHealthRecordResponse;
import com.hms.integration.abdm.dto.AbdmLinkRequest;
import com.hms.integration.abdm.service.AbdmIntegrationService;
import com.hms.patient.dto.AbhaLinkInitiationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "ABDM Integration", description = "ABHA linking, consent management, health record sharing (ABDM gateway itself is stubbed)")
@RestController
@RequestMapping("/api/v1/integration/abdm")
public class AbdmController {

    private final AbdmIntegrationService abdmIntegrationService;

    public AbdmController(AbdmIntegrationService abdmIntegrationService) {
        this.abdmIntegrationService = abdmIntegrationService;
    }

    @Operation(summary = "Initiate ABHA linkage for a patient (reuses the Module 1 OTP flow)")
    @PostMapping("/link")
    public ResponseEntity<ApiResponse<AbhaLinkInitiationResponse>> link(@Valid @RequestBody AbdmLinkRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(abdmIntegrationService.link(request)));
    }

    @Operation(summary = "Grant consent for sharing a patient's health records via ABDM")
    @PostMapping("/consent")
    public ResponseEntity<ApiResponse<AbdmConsentResponse>> consent(@Valid @RequestBody AbdmConsentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(abdmIntegrationService.createConsent(request)));
    }

    @Operation(summary = "Assemble a FHIR Bundle health record for sharing, gated by an active consent")
    @PostMapping("/health-record")
    public ResponseEntity<ApiResponse<AbdmHealthRecordResponse>> healthRecord(
            @Valid @RequestBody AbdmHealthRecordRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(abdmIntegrationService.getHealthRecord(request)));
    }
}
