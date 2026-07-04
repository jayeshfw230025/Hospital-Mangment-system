package com.hms.prescription.web;

import com.hms.common.web.ApiResponse;
import com.hms.prescription.dto.DrugResponse;
import com.hms.prescription.service.DrugService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Drug Master", description = "Drug database search")
@RestController
@RequestMapping("/api/v1/drugs")
public class DrugController {

    private final DrugService drugService;

    public DrugController(DrugService drugService) {
        this.drugService = drugService;
    }

    @Operation(summary = "Search drugs by generic or brand name (autocomplete)")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<DrugResponse>>> search(@RequestParam("q") String query) {
        return ResponseEntity.ok(ApiResponse.ok(drugService.search(query)));
    }
}
