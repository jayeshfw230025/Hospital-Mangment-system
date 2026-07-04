package com.hms.clinical.examination.dto;

public record DigitalRectalExaminationDto(
        Boolean fissures,
        Boolean fistula,
        Boolean externalPiles,
        String sphincterTone,
        Boolean massPresent,
        String massDescription,
        Boolean bloodOnFinger,
        Boolean proctoscopyPerformed,
        String proctoscopyFindings
) {
}
