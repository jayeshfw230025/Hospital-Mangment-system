package com.hms.ipd.procedure.service;

import com.hms.common.exception.ResourceNotFoundException;
import com.hms.ipd.admission.repository.IpdAdmissionRepository;
import com.hms.ipd.procedure.domain.Procedure;
import com.hms.ipd.procedure.domain.ProcedureDetailValidator;
import com.hms.ipd.procedure.domain.ProcedureType;
import com.hms.ipd.procedure.dto.ProcedureRequest;
import com.hms.ipd.procedure.dto.ProcedureResponse;
import com.hms.ipd.procedure.dto.ProcedureTypeResponse;
import com.hms.ipd.procedure.repository.ProcedureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
public class ProcedureService {

    private final ProcedureRepository procedureRepository;
    private final IpdAdmissionRepository ipdAdmissionRepository;
    private final ProcedureDetailValidator procedureDetailValidator;
    private final ProcedureComplicationService procedureComplicationService;

    public ProcedureService(ProcedureRepository procedureRepository,
                             IpdAdmissionRepository ipdAdmissionRepository,
                             ProcedureDetailValidator procedureDetailValidator,
                             ProcedureComplicationService procedureComplicationService) {
        this.procedureRepository = procedureRepository;
        this.ipdAdmissionRepository = ipdAdmissionRepository;
        this.procedureDetailValidator = procedureDetailValidator;
        this.procedureComplicationService = procedureComplicationService;
    }

    @Transactional
    public ProcedureResponse create(ProcedureRequest request) {
        requireAdmissionExists(request.admissionId());
        procedureDetailValidator.validate(request.procedureType(), request.details());

        Procedure procedure = new Procedure();
        procedure.setAdmissionId(request.admissionId());
        applyRequest(procedure, request);

        return toResponse(procedureRepository.save(procedure));
    }

    @Transactional
    public ProcedureResponse update(Long id, ProcedureRequest request) {
        Procedure procedure = procedureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Procedure not found with id: " + id));

        requireAdmissionExists(request.admissionId());
        procedureDetailValidator.validate(request.procedureType(), request.details());

        procedure.setAdmissionId(request.admissionId());
        applyRequest(procedure, request);

        return toResponse(procedureRepository.save(procedure));
    }

    @Transactional(readOnly = true)
    public List<ProcedureResponse> getByAdmissionId(Long admissionId) {
        return procedureRepository.findByAdmissionIdOrderByCreatedAtAsc(admissionId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProcedureTypeResponse> listTypes() {
        return Arrays.stream(ProcedureType.values())
                .map(type -> new ProcedureTypeResponse(type.name(), type.getLabel(), type.getRequiredDetailKeys()))
                .toList();
    }

    private void applyRequest(Procedure procedure, ProcedureRequest request) {
        procedure.setProcedureType(request.procedureType());
        procedure.setProcedureDate(request.procedureDate() == null ? LocalDate.now() : request.procedureDate());
        procedure.setPerformedByName(request.performedByName());
        procedure.setNotes(request.notes());
        procedure.setDetails(request.details());
    }

    private void requireAdmissionExists(Long admissionId) {
        if (ipdAdmissionRepository.findById(admissionId).isEmpty()) {
            throw new ResourceNotFoundException("Admission not found with id: " + admissionId);
        }
    }

    private ProcedureResponse toResponse(Procedure procedure) {
        return new ProcedureResponse(
                procedure.getId(),
                procedure.getAdmissionId(),
                procedure.getProcedureType(),
                procedure.getProcedureType().getLabel(),
                procedure.getProcedureDate(),
                procedure.getPerformedByName(),
                procedure.getNotes(),
                procedure.getDetails(),
                procedureComplicationService.getByProcedureId(procedure.getId()),
                procedure.getCreatedAt()
        );
    }
}
