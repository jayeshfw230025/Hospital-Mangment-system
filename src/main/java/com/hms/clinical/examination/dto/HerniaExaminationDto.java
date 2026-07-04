package com.hms.clinical.examination.dto;

public record HerniaExaminationDto(
        Boolean herniaPresent,
        String site,
        Boolean reducible,
        Boolean coughImpulse
) {
}
