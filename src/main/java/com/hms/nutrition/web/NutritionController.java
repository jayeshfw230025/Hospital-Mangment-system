package com.hms.nutrition.web;

import com.hms.common.web.ApiResponse;
import com.hms.nutrition.dto.CalculateTargetsRequest;
import com.hms.nutrition.dto.CalculateTargetsResponse;
import com.hms.nutrition.dto.NutritionAssessmentRequest;
import com.hms.nutrition.dto.NutritionAssessmentResponse;
import com.hms.nutrition.service.NutritionAssessmentService;
import com.hms.nutrition.service.NutritionCalculatorService;
import com.hms.nutrition.service.NutritionTargets;
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

@Tag(name = "Nutrition & Dietetics", description = "NRS-2002/MUST screening and disease-specific nutrition targets")
@RestController
@RequestMapping("/api/v1/nutrition")
public class NutritionController {

    private final NutritionAssessmentService nutritionAssessmentService;
    private final NutritionCalculatorService nutritionCalculatorService;

    public NutritionController(NutritionAssessmentService nutritionAssessmentService,
                                NutritionCalculatorService nutritionCalculatorService) {
        this.nutritionAssessmentService = nutritionAssessmentService;
        this.nutritionCalculatorService = nutritionCalculatorService;
    }

    @Operation(summary = "Record a nutrition assessment (computes NRS-2002, MUST and disease-specific targets)")
    @PostMapping("/assessment")
    public ResponseEntity<ApiResponse<NutritionAssessmentResponse>> create(
            @Valid @RequestBody NutritionAssessmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Nutrition assessment recorded successfully", nutritionAssessmentService.create(request)));
    }

    @Operation(summary = "Get all nutrition assessments for a patient")
    @GetMapping("/{patientId}")
    public ResponseEntity<ApiResponse<List<NutritionAssessmentResponse>>> getByPatientId(@PathVariable String patientId) {
        return ResponseEntity.ok(ApiResponse.ok(nutritionAssessmentService.getByPatientId(patientId)));
    }

    @Operation(summary = "Update a nutrition assessment")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NutritionAssessmentResponse>> update(
            @PathVariable Long id, @Valid @RequestBody NutritionAssessmentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Nutrition assessment updated successfully",
                nutritionAssessmentService.update(id, request)));
    }

    @Operation(summary = "Calculate disease-specific caloric/protein/fluid targets without saving an assessment")
    @PostMapping("/calculate-targets")
    public ResponseEntity<ApiResponse<CalculateTargetsResponse>> calculateTargets(
            @Valid @RequestBody CalculateTargetsRequest request) {
        NutritionTargets targets = nutritionCalculatorService.calculateTargets(request.diseaseCategory(), request.weightKg());
        return ResponseEntity.ok(ApiResponse.ok(new CalculateTargetsResponse(
                request.diseaseCategory(),
                targets.caloricTargetMinKcalPerDay(), targets.caloricTargetMaxKcalPerDay(),
                targets.proteinTargetMinGPerDay(), targets.proteinTargetMaxGPerDay(),
                targets.fluidRequirementMlPerDay())));
    }
}
