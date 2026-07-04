package com.hms.ipd.web;

import com.hms.common.web.ApiResponse;
import com.hms.ipd.dto.IpdComplaintRequest;
import com.hms.ipd.dto.IpdComplaintResponse;
import com.hms.ipd.service.IpdComplaintService;
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

@Tag(name = "IPD Chief Complaints", description = "Structured GI chief complaint capture for IPD admissions")
@RestController
@RequestMapping("/api/v1/ipd/complaints")
public class IpdComplaintController {

    private final IpdComplaintService ipdComplaintService;

    public IpdComplaintController(IpdComplaintService ipdComplaintService) {
        this.ipdComplaintService = ipdComplaintService;
    }

    @Operation(summary = "Record a chief complaint for an IPD admission")
    @PostMapping
    public ResponseEntity<ApiResponse<IpdComplaintResponse>> create(@Valid @RequestBody IpdComplaintRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Chief complaint recorded successfully", ipdComplaintService.create(request)));
    }

    @Operation(summary = "Get all chief complaints recorded for an IPD admission")
    @GetMapping("/{admissionId}")
    public ResponseEntity<ApiResponse<List<IpdComplaintResponse>>> getByAdmissionId(@PathVariable Long admissionId) {
        return ResponseEntity.ok(ApiResponse.ok(ipdComplaintService.getByAdmissionId(admissionId)));
    }
}
