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
public class HerniaExamination {

    @Column(name = "hernia_present")
    private Boolean herniaPresent;

    @Column(name = "hernia_site")
    private String site;

    @Column(name = "hernia_reducible")
    private Boolean reducible;

    @Column(name = "hernia_cough_impulse")
    private Boolean coughImpulse;
}
