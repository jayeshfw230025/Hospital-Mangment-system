package com.hms.investigation.web;

import com.hms.common.web.ApiResponse;
import com.hms.investigation.dto.InvestigationOrderRequest;
import com.hms.investigation.dto.InvestigationOrderResponse;
import com.hms.investigation.dto.InvestigationReportRequest;
import com.hms.investigation.dto.InvestigationReportResponse;
import com.hms.investigation.service.InvestigationOrderService;
import com.hms.investigation.service.InvestigationReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Investigations", description = "Lab, radiology and imaging investigation orders and reports")
@RestController
@RequestMapping("/api/v1/investigations")
public class InvestigationController {

    private final InvestigationOrderService investigationOrderService;
    private final InvestigationReportService investigationReportService;

    public InvestigationController(InvestigationOrderService investigationOrderService,
                                    InvestigationReportService investigationReportService) {
        this.investigationOrderService = investigationOrderService;
        this.investigationReportService = investigationReportService;
    }

    @Operation(summary = "Order an investigation for a patient (OPD visit or IPD admission)")
    @PostMapping("/order")
    public ResponseEntity<ApiResponse<InvestigationOrderResponse>> order(@Valid @RequestBody InvestigationOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Investigation ordered successfully", investigationOrderService.createOrder(request)));
    }

    @Operation(summary = "Get all investigation orders (with latest report, if any) for a patient")
    @GetMapping("/{patientId}")
    public ResponseEntity<ApiResponse<List<InvestigationOrderResponse>>> getByPatient(@PathVariable String patientId) {
        return ResponseEntity.ok(ApiResponse.ok(investigationOrderService.getByPatientId(patientId)));
    }

    @Operation(summary = "Get all investigation orders for an OPD visit")
    @GetMapping("/visit/{visitId}")
    public ResponseEntity<ApiResponse<List<InvestigationOrderResponse>>> getByVisit(@PathVariable Long visitId) {
        return ResponseEntity.ok(ApiResponse.ok(investigationOrderService.getByVisitId(visitId)));
    }

    @Operation(summary = "Submit results (and optionally a PDF/image report) for an ordered investigation")
    @PostMapping(value = "/report", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<InvestigationReportResponse>> submitReport(
            @Valid @RequestPart("request") InvestigationReportRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Investigation report submitted successfully",
                        investigationReportService.submit(request, file)));
    }

    @Operation(summary = "Download the file attached to an investigation report")
    @GetMapping("/report/{reportId}/download")
    public ResponseEntity<Resource> downloadReport(@PathVariable Long reportId) {
        InvestigationReportService.DownloadableReport downloadable = investigationReportService.download(reportId);

        MediaType mediaType = downloadable.contentType() != null
                ? MediaType.parseMediaType(downloadable.contentType())
                : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(downloadable.fileName()).build().toString())
                .body(downloadable.resource());
    }
}
