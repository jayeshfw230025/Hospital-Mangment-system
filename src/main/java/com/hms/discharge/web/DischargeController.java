package com.hms.discharge.web;

import com.hms.common.web.ApiResponse;
import com.hms.discharge.dto.DischargeSummaryRequest;
import com.hms.discharge.dto.DischargeSummaryResponse;
import com.hms.discharge.dto.WhatsAppDispatchRequest;
import com.hms.discharge.dto.WhatsAppDispatchResponse;
import com.hms.discharge.service.DischargePdfService;
import com.hms.discharge.service.DischargeSummaryService;
import com.hms.discharge.service.WhatsAppDispatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Discharge Summary", description = "Discharge summary generation and WhatsApp dispatch")
@RestController
@RequestMapping("/api/v1/discharge")
public class DischargeController {

    private final DischargeSummaryService dischargeSummaryService;
    private final DischargePdfService dischargePdfService;
    private final WhatsAppDispatchService whatsAppDispatchService;

    public DischargeController(DischargeSummaryService dischargeSummaryService,
                                DischargePdfService dischargePdfService,
                                WhatsAppDispatchService whatsAppDispatchService) {
        this.dischargeSummaryService = dischargeSummaryService;
        this.dischargePdfService = dischargePdfService;
        this.whatsAppDispatchService = whatsAppDispatchService;
    }

    @Operation(summary = "Create a discharge summary (auto-populates procedures, complications, medications, diet plan)")
    @PostMapping
    public ResponseEntity<ApiResponse<DischargeSummaryResponse>> create(@Valid @RequestBody DischargeSummaryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Discharge summary created successfully", dischargeSummaryService.create(request)));
    }

    @Operation(summary = "Get the discharge summary for an admission")
    @GetMapping("/{admissionId}")
    public ResponseEntity<ApiResponse<DischargeSummaryResponse>> getByAdmissionId(@PathVariable Long admissionId) {
        return ResponseEntity.ok(ApiResponse.ok(dischargeSummaryService.getByAdmissionId(admissionId)));
    }

    @Operation(summary = "Download the discharge summary as a PDF")
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        DischargeSummaryResponse summary = dischargeSummaryService.getById(id);
        byte[] pdfBytes = dischargePdfService.generate(summary);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("discharge-summary-" + id + ".pdf").build().toString())
                .body(pdfBytes);
    }

    @Operation(summary = "Dispatch the discharge summary to the patient via WhatsApp")
    @PostMapping("/whatsapp")
    public ResponseEntity<ApiResponse<WhatsAppDispatchResponse>> dispatchWhatsApp(
            @Valid @RequestBody WhatsAppDispatchRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Discharge summary dispatched successfully",
                whatsAppDispatchService.dispatch(request)));
    }

    @Operation(summary = "Update a discharge summary")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DischargeSummaryResponse>> update(@PathVariable Long id,
                                                                         @Valid @RequestBody DischargeSummaryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Discharge summary updated successfully",
                dischargeSummaryService.update(id, request)));
    }
}
