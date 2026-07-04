package com.hms.vitals.service;

import com.hms.vitals.domain.SourceType;
import com.hms.vitals.domain.VitalAlert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Placeholder for the alert-action side effects from Module 4 (nurse station alert,
 * senior doctor escalation, WhatsApp notification). Will be replaced by real Kafka
 * event publishing + WhatsApp Business API integration once the Notification Service
 * stage is implemented; for now these actions are logged so the alert engine's
 * behavior is visible and testable.
 */
@Service
public class VitalAlertNotificationService {

    private static final Logger log = LoggerFactory.getLogger(VitalAlertNotificationService.class);

    public void notifyOfAlert(VitalAlert alert, SourceType sourceType) {
        if (sourceType == SourceType.IPD) {
            log.info("[STUB] Nurse station alerted for patient {} - {}", alert.getPatientUpid(), alert.getMessage());
        }
        log.info("[STUB] Escalating to senior doctor for patient {} - {}", alert.getPatientUpid(), alert.getMessage());
        log.info("[STUB] WhatsApp alert would be sent for patient {} - {} (pending Notification Service integration)",
                alert.getPatientUpid(), alert.getMessage());
    }
}
