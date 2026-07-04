package com.hms.vitals.service;

import com.hms.common.exception.ResourceNotFoundException;
import com.hms.vitals.domain.SourceType;
import com.hms.vitals.domain.VitalAlert;
import com.hms.vitals.dto.VitalAlertResponse;
import com.hms.vitals.repository.VitalAlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class VitalAlertService {

    private final VitalAlertRepository vitalAlertRepository;
    private final VitalAlertNotificationService vitalAlertNotificationService;

    public VitalAlertService(VitalAlertRepository vitalAlertRepository,
                              VitalAlertNotificationService vitalAlertNotificationService) {
        this.vitalAlertRepository = vitalAlertRepository;
        this.vitalAlertNotificationService = vitalAlertNotificationService;
    }

    @Transactional
    public List<VitalAlert> raiseAlerts(String patientUpid, SourceType sourceType, Long sourceVitalsId,
                                         List<TriggeredAlert> triggeredAlerts) {
        return triggeredAlerts.stream()
                .map(triggered -> {
                    VitalAlert alert = new VitalAlert();
                    alert.setPatientUpid(patientUpid);
                    alert.setSourceType(sourceType);
                    alert.setSourceVitalsId(sourceVitalsId);
                    alert.setParameter(triggered.parameter());
                    alert.setMeasuredValue(triggered.measuredValue());
                    alert.setMessage(triggered.message());
                    alert.setAcknowledged(false);

                    VitalAlert saved = vitalAlertRepository.save(alert);
                    vitalAlertNotificationService.notifyOfAlert(saved, sourceType);
                    return saved;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VitalAlertResponse> getByPatientId(String patientId) {
        return vitalAlertRepository.findByPatientUpidOrderByCreatedAtDesc(patientId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VitalAlertResponse> getBySourceVitals(Long sourceVitalsId, SourceType sourceType) {
        return vitalAlertRepository.findBySourceVitalsIdAndSourceType(sourceVitalsId, sourceType).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public VitalAlertResponse acknowledge(Long alertId, String acknowledgedBy) {
        VitalAlert alert = vitalAlertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Vital alert not found with id: " + alertId));

        alert.setAcknowledged(true);
        alert.setAcknowledgedBy(acknowledgedBy);
        alert.setAcknowledgedAt(Instant.now());

        return toResponse(vitalAlertRepository.save(alert));
    }

    public VitalAlertResponse toResponse(VitalAlert alert) {
        return new VitalAlertResponse(
                alert.getId(),
                alert.getPatientUpid(),
                alert.getSourceType(),
                alert.getSourceVitalsId(),
                alert.getParameter(),
                alert.getMeasuredValue(),
                alert.getMessage(),
                alert.isAcknowledged(),
                alert.getAcknowledgedBy(),
                alert.getAcknowledgedAt(),
                alert.getCreatedAt()
        );
    }
}
