package com.hms.analytics.web;

import com.hms.analytics.domain.Granularity;
import com.hms.analytics.dto.AlosEntry;
import com.hms.analytics.dto.DashboardResponse;
import com.hms.analytics.dto.DiseaseDistributionResponse;
import com.hms.analytics.dto.KpisResponse;
import com.hms.analytics.dto.ProcedureStatEntry;
import com.hms.analytics.dto.ReadmissionRateResponse;
import com.hms.analytics.service.AnalyticsExportService;
import com.hms.analytics.service.AnalyticsService;
import com.hms.common.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Analytics", description = "Clinical KPIs and disease distribution dashboard")
@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final AnalyticsExportService analyticsExportService;

    public AnalyticsController(AnalyticsService analyticsService, AnalyticsExportService analyticsExportService) {
        this.analyticsService = analyticsService;
        this.analyticsExportService = analyticsExportService;
    }

    @Operation(summary = "Full dashboard payload: all 10 KPIs + 7-dimension disease distribution")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> dashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "MONTHLY") Granularity granularity) {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getDashboard(startDate, endDate, granularity)));
    }

    @Operation(summary = "The 10 clinical KPIs")
    @GetMapping("/kpis")
    public ResponseEntity<ApiResponse<KpisResponse>> kpis(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "MONTHLY") Granularity granularity) {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getKpis(startDate, endDate, granularity)));
    }

    @Operation(summary = "Disease distribution across the 7 dimensions")
    @GetMapping("/disease-distribution")
    public ResponseEntity<ApiResponse<DiseaseDistributionResponse>> diseaseDistribution() {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getDiseaseDistribution()));
    }

    @Operation(summary = "Average length of stay by diagnosis")
    @GetMapping("/alos")
    public ResponseEntity<ApiResponse<List<AlosEntry>>> alos() {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getAlosByDiagnosis()));
    }

    @Operation(summary = "Re-admission rate (7/14/30-day)")
    @GetMapping("/re-admission")
    public ResponseEntity<ApiResponse<ReadmissionRateResponse>> readmission() {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getReadmissionRate()));
    }

    @Operation(summary = "Procedure success/complication rate by type")
    @GetMapping("/procedure-stats")
    public ResponseEntity<ApiResponse<List<ProcedureStatEntry>>> procedureStats() {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getProcedureStats()));
    }

    @Operation(summary = "Export the KPI dashboard as CSV (opens in Excel) or PDF")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "MONTHLY") Granularity granularity) {
        KpisResponse kpis = analyticsService.getKpis(startDate, endDate, granularity);

        if ("pdf".equalsIgnoreCase(format)) {
            byte[] pdf = analyticsExportService.generatePdf(kpis);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.attachment().filename("analytics-dashboard.pdf").build().toString())
                    .body(pdf);
        }

        byte[] csv = analyticsExportService.generateCsv(kpis);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("analytics-dashboard.csv").build().toString())
                .body(csv);
    }
}
