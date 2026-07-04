package com.hms.cds.web;

import com.hms.cds.dto.BisapScoreRequest;
import com.hms.cds.dto.BisapScoreResponse;
import com.hms.cds.dto.CdaiScoreRequest;
import com.hms.cds.dto.CdaiScoreResponse;
import com.hms.cds.dto.CdsAlertResponse;
import com.hms.cds.dto.CdsAssessRequest;
import com.hms.cds.dto.CtpScoreRequest;
import com.hms.cds.dto.CtpScoreResponse;
import com.hms.cds.dto.MayoScoreRequest;
import com.hms.cds.dto.MayoScoreResponse;
import com.hms.cds.dto.MeldScoreRequest;
import com.hms.cds.dto.MeldScoreResponse;
import com.hms.cds.score.BisapScoreCalculator;
import com.hms.cds.score.CdaiScoreCalculator;
import com.hms.cds.score.CtpScoreCalculator;
import com.hms.cds.score.MayoScoreCalculator;
import com.hms.cds.score.MeldScoreCalculator;
import com.hms.cds.service.CdsAssessmentService;
import com.hms.common.web.ApiResponse;
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

@Tag(name = "Clinical Decision Support", description = "Alert engine and clinical score calculators")
@RestController
@RequestMapping("/api/v1/cds")
public class CdsController {

    private final CdsAssessmentService cdsAssessmentService;
    private final CtpScoreCalculator ctpScoreCalculator;
    private final MeldScoreCalculator meldScoreCalculator;
    private final MayoScoreCalculator mayoScoreCalculator;
    private final BisapScoreCalculator bisapScoreCalculator;
    private final CdaiScoreCalculator cdaiScoreCalculator;

    public CdsController(CdsAssessmentService cdsAssessmentService,
                          CtpScoreCalculator ctpScoreCalculator,
                          MeldScoreCalculator meldScoreCalculator,
                          MayoScoreCalculator mayoScoreCalculator,
                          BisapScoreCalculator bisapScoreCalculator,
                          CdaiScoreCalculator cdaiScoreCalculator) {
        this.cdsAssessmentService = cdsAssessmentService;
        this.ctpScoreCalculator = ctpScoreCalculator;
        this.meldScoreCalculator = meldScoreCalculator;
        this.mayoScoreCalculator = mayoScoreCalculator;
        this.bisapScoreCalculator = bisapScoreCalculator;
        this.cdaiScoreCalculator = cdaiScoreCalculator;
    }

    @Operation(summary = "Run the CDS alert engine for a patient's OPD visit or IPD admission")
    @PostMapping("/assess")
    public ResponseEntity<ApiResponse<List<CdsAlertResponse>>> assess(@Valid @RequestBody CdsAssessRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("CDS assessment completed", cdsAssessmentService.assess(request)));
    }

    @Operation(summary = "Get the CDS alert history for a patient")
    @GetMapping("/alerts/{patientId}")
    public ResponseEntity<ApiResponse<List<CdsAlertResponse>>> getAlerts(@PathVariable String patientId) {
        return ResponseEntity.ok(ApiResponse.ok(cdsAssessmentService.getByPatientId(patientId)));
    }

    @Operation(summary = "Calculate the Child-Turcotte-Pugh (CTP) score")
    @PostMapping("/score/ctp")
    public ResponseEntity<ApiResponse<CtpScoreResponse>> ctp(@Valid @RequestBody CtpScoreRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(ctpScoreCalculator.calculate(request)));
    }

    @Operation(summary = "Calculate the MELD / MELD-Na score")
    @PostMapping("/score/meld")
    public ResponseEntity<ApiResponse<MeldScoreResponse>> meld(@Valid @RequestBody MeldScoreRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(meldScoreCalculator.calculate(request)));
    }

    @Operation(summary = "Calculate the Mayo score for ulcerative colitis")
    @PostMapping("/score/mayo")
    public ResponseEntity<ApiResponse<MayoScoreResponse>> mayo(@Valid @RequestBody MayoScoreRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(mayoScoreCalculator.calculate(request)));
    }

    @Operation(summary = "Calculate the BISAP score for pancreatitis severity")
    @PostMapping("/score/bisap")
    public ResponseEntity<ApiResponse<BisapScoreResponse>> bisap(@Valid @RequestBody BisapScoreRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(bisapScoreCalculator.calculate(request)));
    }

    @Operation(summary = "Calculate the Crohn's Disease Activity Index (CDAI)")
    @PostMapping("/score/cdai")
    public ResponseEntity<ApiResponse<CdaiScoreResponse>> cdai(@Valid @RequestBody CdaiScoreRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(cdaiScoreCalculator.calculate(request)));
    }
}
