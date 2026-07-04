package com.hms.integration.lis.service;

import com.hms.integration.lis.dto.LisImportResponse;
import com.hms.integration.lis.dto.LisStatusResponse;
import com.hms.investigation.dto.InvestigationReportRequest;
import com.hms.investigation.dto.InvestigationReportResponse;
import com.hms.investigation.service.InvestigationReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * There is no real LIS/RIS connection or HL7 v2 parser at this stage - that
 * requires an actual lab/radiology system to integrate against, the same
 * "target infrastructure, deferred" treatment as Kafka/Redis/MinIO. What IS
 * real: once a result has been structured into JSON (as if some other
 * component already parsed the incoming HL7 v2 message), it is persisted
 * through the exact same {@link InvestigationReportService#submit} pipeline
 * a human-entered report would use - no parallel/duplicate business logic.
 */
@Service
public class LisRisIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(LisRisIntegrationService.class);

    private final InvestigationReportService investigationReportService;

    public LisRisIntegrationService(InvestigationReportService investigationReportService) {
        this.investigationReportService = investigationReportService;
    }

    public LisImportResponse importLabResult(InvestigationReportRequest request) {
        InvestigationReportResponse report = investigationReportService.submit(request, null);
        log.info("[STUB] Auto-imported LIS result for order {} (report id {}) - real HL7 v2 parsing/connection pending",
                request.orderId(), report.id());
        return new LisImportResponse("Lab result auto-imported from LIS", report);
    }

    public LisImportResponse importRadiologyResult(InvestigationReportRequest request) {
        InvestigationReportResponse report = investigationReportService.submit(request, null);
        log.info("[STUB] Auto-imported RIS result for order {} (report id {}) - real HL7 v2 parsing/connection pending",
                request.orderId(), report.id());
        return new LisImportResponse("Radiology result auto-imported from RIS", report);
    }

    public LisStatusResponse getStatus() {
        return new LisStatusResponse(false, "No live LIS/RIS connection configured - import endpoints accept "
                + "pre-structured results only");
    }
}
