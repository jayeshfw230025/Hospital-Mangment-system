package com.hms.ipd.procedure.service;

import com.hms.common.exception.ResourceNotFoundException;
import com.hms.ipd.procedure.domain.ProcedureComplication;
import com.hms.ipd.procedure.dto.ProcedureComplicationRequest;
import com.hms.ipd.procedure.dto.ProcedureComplicationResponse;
import com.hms.ipd.procedure.repository.ProcedureComplicationRepository;
import com.hms.ipd.procedure.repository.ProcedureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProcedureComplicationService {

    private final ProcedureComplicationRepository procedureComplicationRepository;
    private final ProcedureRepository procedureRepository;

    public ProcedureComplicationService(ProcedureComplicationRepository procedureComplicationRepository,
                                         ProcedureRepository procedureRepository) {
        this.procedureComplicationRepository = procedureComplicationRepository;
        this.procedureRepository = procedureRepository;
    }

    @Transactional
    public ProcedureComplicationResponse create(ProcedureComplicationRequest request) {
        if (procedureRepository.findById(request.procedureId()).isEmpty()) {
            throw new ResourceNotFoundException("Procedure not found with id: " + request.procedureId());
        }

        ProcedureComplication complication = new ProcedureComplication();
        complication.setProcedureId(request.procedureId());
        complication.setComplicationDescription(request.complicationDescription());
        complication.setSeverity(request.severity());
        complication.setReportedDate(request.reportedDate() == null ? LocalDate.now() : request.reportedDate());
        complication.setReportedByName(request.reportedByName());

        return toResponse(procedureComplicationRepository.save(complication));
    }

    @Transactional(readOnly = true)
    public List<ProcedureComplicationResponse> getByProcedureId(Long procedureId) {
        return procedureComplicationRepository.findByProcedureIdOrderByCreatedAtAsc(procedureId).stream()
                .map(this::toResponse)
                .toList();
    }

    private ProcedureComplicationResponse toResponse(ProcedureComplication complication) {
        return new ProcedureComplicationResponse(
                complication.getId(), complication.getProcedureId(), complication.getComplicationDescription(),
                complication.getSeverity(), complication.getReportedDate(), complication.getReportedByName(),
                complication.getCreatedAt());
    }
}
