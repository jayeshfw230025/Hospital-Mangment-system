package com.hms.ipd.admission.dto;

import com.hms.ipd.admission.domain.BedStatus;
import com.hms.ipd.admission.domain.WardType;

public record BedResponse(
        Long id,
        WardType wardType,
        String roomNumber,
        String bedNumber,
        BedStatus status,
        Long currentAdmissionId
) {
}
