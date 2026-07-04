package com.hms.ipd.admission.web;

import com.hms.common.web.ApiResponse;
import com.hms.ipd.admission.dto.BedResponse;
import com.hms.ipd.admission.dto.BedTransferRequest;
import com.hms.ipd.admission.dto.IpdAdmissionRequest;
import com.hms.ipd.admission.dto.IpdAdmissionResponse;
import com.hms.ipd.admission.service.BedService;
import com.hms.ipd.admission.service.IpdAdmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "IPD Admission", description = "IPD admission records, bed transfer")
@RestController
@RequestMapping("/api/v1/ipd/admission")
public class IpdAdmissionController {

    private final IpdAdmissionService ipdAdmissionService;
    private final BedService bedService;

    public IpdAdmissionController(IpdAdmissionService ipdAdmissionService, BedService bedService) {
        this.ipdAdmissionService = ipdAdmissionService;
        this.bedService = bedService;
    }

    @Operation(summary = "Create an IPD admission (auto-populates allergies/current medications from patient history)")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<IpdAdmissionResponse>> create(
            @Valid @RequestPart("request") IpdAdmissionRequest request,
            @RequestPart(value = "consentDocument", required = false) MultipartFile consentDocument) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Admission created successfully", ipdAdmissionService.create(request, consentDocument)));
    }

    @Operation(summary = "Get an IPD admission by id")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IpdAdmissionResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(ipdAdmissionService.getById(id)));
    }

    @Operation(summary = "Update an IPD admission")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<IpdAdmissionResponse>> update(
            @PathVariable Long id,
            @Valid @RequestPart("request") IpdAdmissionRequest request,
            @RequestPart(value = "consentDocument", required = false) MultipartFile consentDocument) {
        return ResponseEntity.ok(ApiResponse.ok("Admission updated successfully",
                ipdAdmissionService.update(id, request, consentDocument)));
    }

    @Operation(summary = "Transfer an admission's patient to a different bed/ward")
    @PostMapping(value = "/transfer", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<BedResponse>> transfer(@Valid @RequestBody BedTransferRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Patient transferred successfully", bedService.transfer(request)));
    }
}
