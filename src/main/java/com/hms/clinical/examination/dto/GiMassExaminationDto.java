package com.hms.clinical.examination.dto;

import com.hms.clinical.examination.MassConsistency;
import com.hms.clinical.examination.MassMobility;

public record GiMassExaminationDto(
        Boolean massPresent,
        String location,
        Double sizeCm,
        MassMobility mobility,
        MassConsistency consistency
) {
}
