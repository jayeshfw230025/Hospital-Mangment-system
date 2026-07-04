package com.hms.discharge.dto;

import com.hms.discharge.domain.DispatchStatus;

import java.time.Instant;

public record WhatsAppDispatchResponse(
        Long dischargeId,
        String phoneNumberUsed,
        DispatchStatus dispatchStatus,
        String messageId,
        String qrCodeBase64,
        boolean followUpReminderScheduled,
        Instant followUpReminderDateTime
) {
}
