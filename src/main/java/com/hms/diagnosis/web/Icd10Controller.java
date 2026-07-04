package com.hms.diagnosis.web;

import com.hms.common.web.ApiResponse;
import com.hms.diagnosis.dto.Icd10CodeResponse;
import com.hms.diagnosis.service.Icd10CodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "ICD-10 Lookup", description = "Pre-configured GI ICD-10 code search and listing")
@RestController
@RequestMapping("/api/v1/icd10")
public class Icd10Controller {

    private final Icd10CodeService icd10CodeService;

    public Icd10Controller(Icd10CodeService icd10CodeService) {
        this.icd10CodeService = icd10CodeService;
    }

    @Operation(summary = "Search ICD-10 codes by code or description")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Icd10CodeResponse>>> search(@RequestParam("q") String query) {
        return ResponseEntity.ok(ApiResponse.ok(icd10CodeService.search(query)));
    }

    @Operation(summary = "List all pre-configured GI ICD-10 codes")
    @GetMapping("/gastro-codes")
    public ResponseEntity<ApiResponse<List<Icd10CodeResponse>>> getGastroCodes() {
        return ResponseEntity.ok(ApiResponse.ok(icd10CodeService.getGastroCodes()));
    }
}
