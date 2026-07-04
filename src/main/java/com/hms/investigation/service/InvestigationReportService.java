package com.hms.investigation.service;

import com.hms.common.exception.ResourceNotFoundException;
import com.hms.investigation.domain.InvestigationOrder;
import com.hms.investigation.domain.InvestigationReport;
import com.hms.investigation.domain.InvestigationType;
import com.hms.investigation.domain.OrderStatus;
import com.hms.investigation.domain.ResultParameter;
import com.hms.investigation.dto.InvestigationReportRequest;
import com.hms.investigation.dto.InvestigationReportResponse;
import com.hms.investigation.dto.ResultParameterRequest;
import com.hms.investigation.dto.ResultParameterResponse;
import com.hms.investigation.repository.InvestigationOrderRepository;
import com.hms.investigation.repository.InvestigationReportRepository;
import com.hms.investigation.repository.InvestigationTypeRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class InvestigationReportService {

    private static final int TREND_HISTORY_SIZE = 2;

    private final InvestigationReportRepository investigationReportRepository;
    private final InvestigationOrderRepository investigationOrderRepository;
    private final InvestigationTypeRepository investigationTypeRepository;
    private final ReportFileStorageService reportFileStorageService;

    public InvestigationReportService(InvestigationReportRepository investigationReportRepository,
                                       InvestigationOrderRepository investigationOrderRepository,
                                       InvestigationTypeRepository investigationTypeRepository,
                                       ReportFileStorageService reportFileStorageService) {
        this.investigationReportRepository = investigationReportRepository;
        this.investigationOrderRepository = investigationOrderRepository;
        this.investigationTypeRepository = investigationTypeRepository;
        this.reportFileStorageService = reportFileStorageService;
    }

    @Transactional
    public InvestigationReportResponse submit(InvestigationReportRequest request, MultipartFile file) {
        InvestigationOrder order = investigationOrderRepository.findById(request.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Investigation order not found with id: " + request.orderId()));

        InvestigationReport report = new InvestigationReport();
        report.setOrderId(order.getId());
        report.setPatientUpid(order.getPatientUpid());
        report.setInvestigationTypeCode(order.getInvestigationTypeCode());
        report.setReportDate(request.reportDate() == null ? LocalDate.now() : request.reportDate());
        report.setNotes(request.notes());
        report.setResultParameters(request.resultParameters().stream().map(this::toResultParameter).toList());

        if (file != null && !file.isEmpty()) {
            report.setReportFileKey(reportFileStorageService.store(file));
            report.setReportFileName(file.getOriginalFilename());
            report.setReportContentType(file.getContentType());
        }

        InvestigationReport saved = investigationReportRepository.save(report);

        order.setStatus(OrderStatus.COMPLETED);
        investigationOrderRepository.save(order);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public InvestigationReportResponse getById(Long reportId) {
        return toResponse(findReportOrThrow(reportId));
    }

    @Transactional(readOnly = true)
    public DownloadableReport download(Long reportId) {
        InvestigationReport report = findReportOrThrow(reportId);
        if (report.getReportFileKey() == null) {
            throw new ResourceNotFoundException("No file attached to report with id: " + reportId);
        }
        Resource resource = reportFileStorageService.load(report.getReportFileKey());
        return new DownloadableReport(resource, report.getReportFileName(), report.getReportContentType());
    }

    @Transactional(readOnly = true)
    public Optional<InvestigationReportResponse> getLatestForOrder(Long orderId) {
        return investigationReportRepository.findFirstByOrderIdOrderByCreatedAtDesc(orderId).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<InvestigationReportResponse> getByPatientId(String patientUpid) {
        return investigationReportRepository.findByPatientUpidOrderByReportDateDescIdDesc(patientUpid).stream()
                .map(this::toResponse)
                .toList();
    }

    private ResultParameter toResultParameter(ResultParameterRequest request) {
        boolean abnormal = computeAbnormal(request);
        return new ResultParameter(request.parameterName(), request.value(), request.unit(),
                request.referenceRangeLow(), request.referenceRangeHigh(), abnormal);
    }

    private boolean computeAbnormal(ResultParameterRequest request) {
        if (request.referenceRangeLow() != null && request.referenceRangeHigh() != null) {
            try {
                double numericValue = Double.parseDouble(request.value());
                return numericValue < request.referenceRangeLow() || numericValue > request.referenceRangeHigh();
            } catch (NumberFormatException ignored) {
                // non-numeric value with a numeric reference range; fall through to explicit override
            }
        }
        return Boolean.TRUE.equals(request.abnormalOverride());
    }

    private InvestigationReport findReportOrThrow(Long reportId) {
        return investigationReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Investigation report not found with id: " + reportId));
    }

    private InvestigationReportResponse toResponse(InvestigationReport report) {
        String investigationName = investigationTypeRepository.findById(report.getInvestigationTypeCode())
                .map(InvestigationType::getName)
                .orElse(null);

        List<ResultParameterResponse> parameterResponses = report.getResultParameters().stream()
                .map(param -> new ResultParameterResponse(
                        param.getParameterName(),
                        param.getValue(),
                        param.getUnit(),
                        param.getReferenceRangeLow(),
                        param.getReferenceRangeHigh(),
                        param.isAbnormal(),
                        getPreviousValues(report, param.getParameterName())))
                .toList();

        return new InvestigationReportResponse(
                report.getId(),
                report.getOrderId(),
                report.getPatientUpid(),
                report.getInvestigationTypeCode(),
                investigationName,
                report.getReportDate(),
                parameterResponses,
                report.getReportFileKey() != null,
                report.getReportFileName(),
                report.getNotes(),
                report.getCreatedAt()
        );
    }

    private List<String> getPreviousValues(InvestigationReport currentReport, String parameterName) {
        return investigationReportRepository
                .findByPatientUpidAndInvestigationTypeCodeOrderByReportDateDescIdDesc(
                        currentReport.getPatientUpid(), currentReport.getInvestigationTypeCode())
                .stream()
                .filter(r -> !r.getId().equals(currentReport.getId()))
                .flatMap(r -> r.getResultParameters().stream()
                        .filter(p -> p.getParameterName().equals(parameterName))
                        .map(ResultParameter::getValue))
                .limit(TREND_HISTORY_SIZE)
                .toList();
    }

    public record DownloadableReport(Resource resource, String fileName, String contentType) {
    }
}
