package com.hms.clinical.examination.dto;

import com.hms.clinical.examination.BowelSounds;

public record AbdominalExaminationDto(
        Boolean scarsPresent,
        Boolean distensionPresent,
        Boolean visiblePeristalsis,
        Boolean tenderness,
        String tendernessSite,
        Boolean guarding,
        Boolean rigidity,
        String organomegaly,
        Boolean percussionDullness,
        Boolean tympanic,
        BowelSounds bowelSounds,
        String notes
) {
}
