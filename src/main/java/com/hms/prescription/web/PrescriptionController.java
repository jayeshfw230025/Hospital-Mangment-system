package com.hms.prescription.web;

import com.hms.common.web.ApiResponse;
import com.hms.prescription.domain.Prescription;
import com.hms.prescription.dto.DrugInteractionCheckRequest;
import com.hms.prescription.dto.DrugInteractionCheckResponse;
import com.hms.prescription.dto.GeneratePdfRequest;
import com.hms.prescription.dto.PrescriptionRequest;
import com.hms.prescription.dto.PrescriptionResponse;
import com.hms.prescription.dto.TemplateResponse;
import com.hms.prescription.service.PrescriptionPdfService;
import com.hms.prescription.service.PrescriptionService;
import com.hms.prescription.service.TemplateService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Prescriptions", description = "E-prescription with gastro templates, safety checks and PDF generation")
@RestController
@RequestMapping("/api/v1/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final TemplateService templateService;
    private final PrescriptionPdfService prescriptionPdfService;

    public PrescriptionController(PrescriptionService prescriptionService,
                                   TemplateService templateService,
                                   PrescriptionPdfService prescriptionPdfService) {
        this.prescriptionService = prescriptionService;
        this.templateService = templateService;
        this.prescriptionPdfService = prescriptionPdfService;
    }

    @Operation(summary = "Create a prescription (runs allergy hard-stop, interaction and nutrition checks)")
    @PostMapping
    public ResponseEntity<ApiResponse<PrescriptionResponse>> create(@Valid @RequestBody PrescriptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Prescription created successfully", prescriptionService.create(request)));
    }

    @Operation(summary = "Get all prescriptions for a patient")
    @GetMapping("/{patientId}")
    public ResponseEntity<ApiResponse<List<PrescriptionResponse>>> getByPatientId(@PathVariable String patientId) {
        return ResponseEntity.ok(ApiResponse.ok(prescriptionService.getByPatientId(patientId)));
    }

    @Operation(summary = "List the 11 gastro prescription templates with suggested drugs")
    @GetMapping("/templates")
    public ResponseEntity<ApiResponse<List<TemplateResponse>>> getTemplates() {
        return ResponseEntity.ok(ApiResponse.ok(templateService.listTemplates()));
    }

    @Operation(summary = "Generate a PDF for a prescription")
    @PostMapping("/generate-pdf")
    public ResponseEntity<byte[]> generatePdf(@Valid @RequestBody GeneratePdfRequest request) {
        Prescription prescription = prescriptionService.getEntityById(request.prescriptionId());
        PrescriptionPdfService.GeneratedPdf generated = prescriptionPdfService.generate(prescription);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(generated.fileName()).build().toString())
                .body(generated.bytes());
    }

    @Operation(summary = "Check drug interactions and nutrition alerts for a set of drugs")
    @PostMapping("/check-interactions")
    public ResponseEntity<ApiResponse<DrugInteractionCheckResponse>> checkInteractions(
            @Valid @RequestBody DrugInteractionCheckRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(prescriptionService.checkInteractions(request)));
    }
}
