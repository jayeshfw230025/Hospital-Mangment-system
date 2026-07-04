package com.hms.ipd.admission.web;

import com.hms.common.web.ApiResponse;
import com.hms.ipd.admission.dto.TpaPreAuthRequest;
import com.hms.ipd.admission.dto.TpaPreAuthResponse;
import com.hms.ipd.admission.service.TpaPreAuthorizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "TPA Pre-authorization", description = "Insurance pre-authorization for IPD admissions")
@RestController
@RequestMapping("/api/v1/ipd/tpa")
public class TpaController {

    private final TpaPreAuthorizationService tpaPreAuthorizationService;

    public TpaController(TpaPreAuthorizationService tpaPreAuthorizationService) {
        this.tpaPreAuthorizationService = tpaPreAuthorizationService;
    }

    @Operation(summary = "Submit a TPA pre-authorization request for an admission")
    @PostMapping("/preauth")
    public ResponseEntity<ApiResponse<TpaPreAuthResponse>> submit(@Valid @RequestBody TpaPreAuthRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("TPA pre-authorization submitted successfully", tpaPreAuthorizationService.submit(request)));
    }
}
