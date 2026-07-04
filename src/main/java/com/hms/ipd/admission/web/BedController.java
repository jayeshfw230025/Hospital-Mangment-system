package com.hms.ipd.admission.web;

import com.hms.common.web.ApiResponse;
import com.hms.ipd.admission.domain.WardType;
import com.hms.ipd.admission.dto.BedAllocateRequest;
import com.hms.ipd.admission.dto.BedResponse;
import com.hms.ipd.admission.service.BedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Bed Management", description = "Bed allocation and availability")
@RestController
@RequestMapping("/api/v1/ipd/bed")
public class BedController {

    private final BedService bedService;

    public BedController(BedService bedService) {
        this.bedService = bedService;
    }

    @Operation(summary = "Allocate a bed to an admission")
    @PostMapping("/allocate")
    public ResponseEntity<ApiResponse<BedResponse>> allocate(@Valid @RequestBody BedAllocateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Bed allocated successfully", bedService.allocate(request)));
    }

    @Operation(summary = "List available beds, optionally filtered by ward type")
    @GetMapping("/availability")
    public ResponseEntity<ApiResponse<List<BedResponse>>> availability(
            @RequestParam(required = false) WardType wardType) {
        return ResponseEntity.ok(ApiResponse.ok(bedService.getAvailableBeds(wardType)));
    }
}
