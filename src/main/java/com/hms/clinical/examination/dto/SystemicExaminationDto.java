package com.hms.clinical.examination.dto;

import com.hms.clinical.examination.PupillaryReflex;

public record SystemicExaminationDto(
        String chestExpansion,
        String breathSounds,
        String heartSounds,
        Boolean murmursPresent,
        String murmurDescription,
        String jvp,
        Integer gcsScore,
        PupillaryReflex pupillaryReflex,
        String motorFindings,
        String sensoryFindings
) {
}
