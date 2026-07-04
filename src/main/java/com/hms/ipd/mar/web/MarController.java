package com.hms.ipd.mar.web;

import com.hms.common.web.ApiResponse;
import com.hms.ipd.mar.dto.MarRequest;
import com.hms.ipd.mar.dto.MarResponse;
import com.hms.ipd.mar.service.MarService;
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

@Tag(name = "Medication Administration Record", description = "Nursing MAR charting for IPD admissions")
@RestController
@RequestMapping("/api/v1/ipd/mar")
public class MarController {

    private final MarService marService;

    public MarController(MarService marService) {
        this.marService = marService;
    }

    @Operation(summary = "Record a medication administration entry")
    @PostMapping
    public ResponseEntity<ApiResponse<MarResponse>> create(@Valid @RequestBody MarRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Medication administration recorded successfully", marService.create(request)));
    }

    @Operation(summary = "Get the medication administration record for an admission")
    @GetMapping("/{admissionId}")
    public ResponseEntity<ApiResponse<List<MarResponse>>> getByAdmissionId(@PathVariable Long admissionId) {
        return ResponseEntity.ok(ApiResponse.ok(marService.getByAdmissionId(admissionId)));
    }
}
