package com.hms.discharge.service;

import com.hms.common.exception.ResourceNotFoundException;
import com.hms.discharge.domain.DispatchStatus;
import com.hms.discharge.dto.DischargeSummaryResponse;
import com.hms.discharge.dto.WhatsAppDispatchRequest;
import com.hms.discharge.dto.WhatsAppDispatchResponse;
import com.hms.patient.domain.Patient;
import com.hms.patient.repository.PatientRepository;
import com.hms.patient.service.QrCodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Placeholder for the Meta WhatsApp Business API integration from Stage A, which
 * has not been built yet - logs what would be sent/scheduled so the dispatch
 * flow's behavior is visible and testable, consistent with the other deferred
 * integration stubs across this project (ABHA linkage, vital alert notifications).
 */
@Service
public class WhatsAppDispatchService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppDispatchService.class);

    private final DischargeSummaryService dischargeSummaryService;
    private final PatientRepository patientRepository;
    private final DischargePdfService dischargePdfService;
    private final QrCodeService qrCodeService;

    public WhatsAppDispatchService(DischargeSummaryService dischargeSummaryService,
                                    PatientRepository patientRepository,
                                    DischargePdfService dischargePdfService,
                                    QrCodeService qrCodeService) {
        this.dischargeSummaryService = dischargeSummaryService;
        this.patientRepository = patientRepository;
        this.dischargePdfService = dischargePdfService;
        this.qrCodeService = qrCodeService;
    }

    @Transactional(readOnly = true)
    public WhatsAppDispatchResponse dispatch(WhatsAppDispatchRequest request) {
        DischargeSummaryResponse summary = dischargeSummaryService.getById(request.dischargeId());

        String phoneNumber = request.phoneNumberOverride() != null
                ? request.phoneNumberOverride()
                : resolvePatientContactNumber(summary.patientId());

        byte[] pdfBytes = dischargePdfService.generate(summary);
        log.info("[STUB] Generated discharge summary PDF ({} bytes) for patient {}", pdfBytes.length, summary.patientId());

        String qrContent = summary.patientId() + "|discharge:" + summary.id();
        String qrCodeBase64 = qrCodeService.generateBase64Png(qrContent);

        String messageId = UUID.randomUUID().toString();
        log.info("[STUB] WhatsApp discharge summary would be sent to {} for patient {} (message id {})",
                phoneNumber, summary.patientId(), messageId);

        boolean reminderScheduled = summary.followUpDateTime() != null;
        if (reminderScheduled) {
            log.info("[STUB] Follow-up reminder would be scheduled for patient {} at {}",
                    summary.patientId(), summary.followUpDateTime());
        }

        return new WhatsAppDispatchResponse(
                summary.id(), phoneNumber, DispatchStatus.SENT, messageId, qrCodeBase64,
                reminderScheduled, summary.followUpDateTime());
    }

    private String resolvePatientContactNumber(String patientUpid) {
        Patient patient = patientRepository.findByUpid(patientUpid)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with UPID: " + patientUpid));
        return patient.getPrimaryContactNumber();
    }
}
