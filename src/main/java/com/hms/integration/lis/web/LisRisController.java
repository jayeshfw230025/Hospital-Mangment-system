package com.hms.integration.lis.web;

import com.hms.common.web.ApiResponse;
import com.hms.integration.lis.dto.LisImportResponse;
import com.hms.integration.lis.dto.LisStatusResponse;
import com.hms.integration.lis.service.LisRisIntegrationService;
import com.hms.investigation.dto.InvestigationReportRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "LIS/RIS Integration", description = "Auto-population of lab and radiology results (no live LIS/RIS connection at this stage)")
@RestController
@RequestMapping("/api/v1/integration")
public class LisRisController {

    private final LisRisIntegrationService lisRisIntegrationService;

    public LisRisController(LisRisIntegrationService lisRisIntegrationService) {
        this.lisRisIntegrationService = lisRisIntegrationService;
    }

    @Operation(summary = "Import a pre-structured lab result against an existing investigation order")
    @PostMapping("/lis/import")
    public ResponseEntity<ApiResponse<LisImportResponse>> importLis(@Valid @RequestBody InvestigationReportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(lisRisIntegrationService.importLabResult(request)));
    }

    @Operation(summary = "LIS connection status")
    @GetMapping("/lis/status")
    public ResponseEntity<ApiResponse<LisStatusResponse>> lisStatus() {
        return ResponseEntity.ok(ApiResponse.ok(lisRisIntegrationService.getStatus()));
    }

    @Operation(summary = "Import a pre-structured radiology result against an existing investigation order")
    @PostMapping("/ris/import")
    public ResponseEntity<ApiResponse<LisImportResponse>> importRis(@Valid @RequestBody InvestigationReportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(lisRisIntegrationService.importRadiologyResult(request)));
    }
}
