package com.hms.opd.web;

import com.hms.common.web.ApiResponse;
import com.hms.opd.dto.OpdComplaintRequest;
import com.hms.opd.dto.OpdComplaintResponse;
import com.hms.opd.service.OpdComplaintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "OPD Chief Complaints", description = "Structured GI chief complaint capture for OPD visits")
@RestController
@RequestMapping("/api/v1/opd/complaints")
public class OpdComplaintController {

    private final OpdComplaintService opdComplaintService;

    public OpdComplaintController(OpdComplaintService opdComplaintService) {
        this.opdComplaintService = opdComplaintService;
    }

    @Operation(summary = "Record a chief complaint for an OPD visit")
    @PostMapping
    public ResponseEntity<ApiResponse<OpdComplaintResponse>> create(@Valid @RequestBody OpdComplaintRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Chief complaint recorded successfully", opdComplaintService.create(request)));
    }

    @Operation(summary = "Get all chief complaints recorded for an OPD visit")
    @GetMapping("/{visitId}")
    public ResponseEntity<ApiResponse<List<OpdComplaintResponse>>> getByVisitId(@PathVariable Long visitId) {
        return ResponseEntity.ok(ApiResponse.ok(opdComplaintService.getByVisitId(visitId)));
    }

    @Operation(summary = "Update a previously recorded chief complaint")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OpdComplaintResponse>> update(@PathVariable Long id,
                                                                     @Valid @RequestBody OpdComplaintRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Chief complaint updated successfully",
                opdComplaintService.update(id, request)));
    }
}
