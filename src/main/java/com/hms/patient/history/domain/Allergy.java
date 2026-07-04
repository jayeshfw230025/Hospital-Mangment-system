package com.hms.patient.history.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Allergy {

    @Column(name = "allergen")
    private String allergen;

    @Column(name = "reaction_type")
    private String reactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", length = 15)
    private AllergySeverity severity;

    @Column(name = "hard_stop")
    private boolean hardStop;
}
