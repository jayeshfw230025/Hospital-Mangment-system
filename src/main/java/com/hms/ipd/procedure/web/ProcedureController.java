package com.hms.ipd.procedure.web;

import com.hms.common.web.ApiResponse;
import com.hms.ipd.procedure.dto.ProcedureComplicationRequest;
import com.hms.ipd.procedure.dto.ProcedureComplicationResponse;
import com.hms.ipd.procedure.dto.ProcedureRequest;
import com.hms.ipd.procedure.dto.ProcedureResponse;
import com.hms.ipd.procedure.dto.ProcedureTypeResponse;
import com.hms.ipd.procedure.service.ProcedureComplicationService;
import com.hms.ipd.procedure.service.ProcedureService;
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

@Tag(name = "IPD Procedures", description = "Structured capture for the 11 IPD procedure types")
@RestController
@RequestMapping("/api/v1/ipd/procedure")
public class ProcedureController {

    private final ProcedureService procedureService;
    private final ProcedureComplicationService procedureComplicationService;

    public ProcedureController(ProcedureService procedureService,
                                ProcedureComplicationService procedureComplicationService) {
        this.procedureService = procedureService;
        this.procedureComplicationService = procedureComplicationService;
    }

    @Operation(summary = "Record a procedure for an IPD admission")
    @PostMapping
    public ResponseEntity<ApiResponse<ProcedureResponse>> create(@Valid @RequestBody ProcedureRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Procedure recorded successfully", procedureService.create(request)));
    }

    @Operation(summary = "Get all procedures recorded for an admission")
    @GetMapping("/{admissionId}")
    public ResponseEntity<ApiResponse<List<ProcedureResponse>>> getByAdmissionId(@PathVariable Long admissionId) {
        return ResponseEntity.ok(ApiResponse.ok(procedureService.getByAdmissionId(admissionId)));
    }

    @Operation(summary = "List the 11 supported procedure types and their key fields")
    @GetMapping("/types")
    public ResponseEntity<ApiResponse<List<ProcedureTypeResponse>>> listTypes() {
        return ResponseEntity.ok(ApiResponse.ok(procedureService.listTypes()));
    }

    @Operation(summary = "Report a complication against a previously recorded procedure")
    @PostMapping("/complication")
    public ResponseEntity<ApiResponse<ProcedureComplicationResponse>> reportComplication(
            @Valid @RequestBody ProcedureComplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Complication recorded successfully", procedureComplicationService.create(request)));
    }

    @Operation(summary = "Update a recorded procedure")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProcedureResponse>> update(@PathVariable Long id,
                                                                  @Valid @RequestBody ProcedureRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Procedure updated successfully", procedureService.update(id, request)));
    }
}
