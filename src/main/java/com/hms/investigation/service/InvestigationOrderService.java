package com.hms.investigation.service;

import com.hms.common.exception.ResourceNotFoundException;
import com.hms.investigation.domain.InvestigationOrder;
import com.hms.investigation.domain.InvestigationType;
import com.hms.investigation.domain.OrderStatus;
import com.hms.investigation.dto.InvestigationOrderRequest;
import com.hms.investigation.dto.InvestigationOrderResponse;
import com.hms.investigation.repository.InvestigationOrderRepository;
import com.hms.investigation.repository.InvestigationTypeRepository;
import com.hms.patient.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class InvestigationOrderService {

    private final InvestigationOrderRepository investigationOrderRepository;
    private final InvestigationTypeRepository investigationTypeRepository;
    private final PatientRepository patientRepository;
    private final InvestigationReportService investigationReportService;

    public InvestigationOrderService(InvestigationOrderRepository investigationOrderRepository,
                                      InvestigationTypeRepository investigationTypeRepository,
                                      PatientRepository patientRepository,
                                      InvestigationReportService investigationReportService) {
        this.investigationOrderRepository = investigationOrderRepository;
        this.investigationTypeRepository = investigationTypeRepository;
        this.patientRepository = patientRepository;
        this.investigationReportService = investigationReportService;
    }

    @Transactional
    public InvestigationOrderResponse createOrder(InvestigationOrderRequest request) {
        if (request.visitId() == null && request.admissionId() == null) {
            throw new IllegalArgumentException("Either visitId or admissionId must be provided");
        }
        if (patientRepository.findByUpid(request.patientId()).isEmpty()) {
            throw new ResourceNotFoundException("Patient not found with UPID: " + request.patientId());
        }

        InvestigationType investigationType = investigationTypeRepository.findById(request.investigationTypeCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Investigation type not found: " + request.investigationTypeCode()));
        if (!investigationType.isActive()) {
            throw new IllegalArgumentException("Investigation type " + request.investigationTypeCode() + " is inactive");
        }
        if (request.visitId() != null && investigationType.isIpdOnly()) {
            throw new IllegalArgumentException(
                    "Investigation type " + request.investigationTypeCode() + " is only available for IPD admissions");
        }

        InvestigationOrder order = new InvestigationOrder();
        order.setPatientUpid(request.patientId());
        order.setVisitId(request.visitId());
        order.setAdmissionId(request.admissionId());
        order.setInvestigationTypeCode(investigationType.getCode());
        order.setOrderedDate(LocalDate.now());
        order.setStatus(OrderStatus.ORDERED);
        order.setNotes(request.notes());

        return toResponse(investigationOrderRepository.save(order), investigationType);
    }

    @Transactional(readOnly = true)
    public List<InvestigationOrderResponse> getByPatientId(String patientId) {
        return investigationOrderRepository.findByPatientUpidOrderByCreatedAtDesc(patientId).stream()
                .map(order -> toResponse(order, investigationTypeRepository.findById(order.getInvestigationTypeCode()).orElse(null)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InvestigationOrderResponse> getByVisitId(Long visitId) {
        return investigationOrderRepository.findByVisitIdOrderByCreatedAtDesc(visitId).stream()
                .map(order -> toResponse(order, investigationTypeRepository.findById(order.getInvestigationTypeCode()).orElse(null)))
                .toList();
    }

    private InvestigationOrderResponse toResponse(InvestigationOrder order, InvestigationType investigationType) {
        return new InvestigationOrderResponse(
                order.getId(),
                order.getPatientUpid(),
                order.getVisitId(),
                order.getAdmissionId(),
                order.getInvestigationTypeCode(),
                investigationType == null ? null : investigationType.getName(),
                investigationType == null ? null : investigationType.getCategory(),
                order.getOrderedDate(),
                order.getStatus(),
                order.getNotes(),
                investigationReportService.getLatestForOrder(order.getId()).orElse(null),
                order.getCreatedAt()
        );
    }
}
