package com.hms.ipd.progressnote.web;

import com.hms.common.web.ApiResponse;
import com.hms.ipd.progressnote.dto.ProgressNoteRequest;
import com.hms.ipd.progressnote.dto.ProgressNoteResponse;
import com.hms.ipd.progressnote.service.ProgressNoteService;
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

@Tag(name = "IPD Daily Progress Notes", description = "SOAP-format daily progress notes for IPD admissions")
@RestController
@RequestMapping("/api/v1/ipd/progress-note")
public class ProgressNoteController {

    private final ProgressNoteService progressNoteService;

    public ProgressNoteController(ProgressNoteService progressNoteService) {
        this.progressNoteService = progressNoteService;
    }

    @Operation(summary = "Record a daily SOAP progress note for an IPD admission")
    @PostMapping
    public ResponseEntity<ApiResponse<ProgressNoteResponse>> create(@Valid @RequestBody ProgressNoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Progress note recorded successfully", progressNoteService.create(request)));
    }

    @Operation(summary = "Get all progress notes for an admission")
    @GetMapping("/{admissionId}")
    public ResponseEntity<ApiResponse<List<ProgressNoteResponse>>> getByAdmissionId(@PathVariable Long admissionId) {
        return ResponseEntity.ok(ApiResponse.ok(progressNoteService.getByAdmissionId(admissionId)));
    }

    @Operation(summary = "Update a progress note")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProgressNoteResponse>> update(@PathVariable Long id,
                                                                     @Valid @RequestBody ProgressNoteRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Progress note updated successfully",
                progressNoteService.update(id, request)));
    }
}
