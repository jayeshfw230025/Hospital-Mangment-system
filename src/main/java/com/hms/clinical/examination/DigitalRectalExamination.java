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
public class DigitalRectalExamination {

    @Column(name = "dre_fissures")
    private Boolean fissures;

    @Column(name = "dre_fistula")
    private Boolean fistula;

    @Column(name = "dre_external_piles")
    private Boolean externalPiles;

    @Column(name = "dre_sphincter_tone")
    private String sphincterTone;

    @Column(name = "dre_mass_present")
    private Boolean massPresent;

    @Column(name = "dre_mass_description")
    private String massDescription;

    @Column(name = "dre_blood_on_finger")
    private Boolean bloodOnFinger;

    @Column(name = "dre_proctoscopy_performed")
    private Boolean proctoscopyPerformed;

    @Column(name = "dre_proctoscopy_findings", length = 500)
    private String proctoscopyFindings;
}
