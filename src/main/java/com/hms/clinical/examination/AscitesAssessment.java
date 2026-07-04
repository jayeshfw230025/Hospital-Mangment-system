package com.hms.clinical.examination;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class AscitesAssessment {

    @Column(name = "ascites_shifting_dullness")
    private Boolean shiftingDullnessPresent;

    @Column(name = "ascites_fluid_thrill")
    private Boolean fluidThrillPresent;

    @Column(name = "ascites_notes", length = 500)
    private String notes;
}
