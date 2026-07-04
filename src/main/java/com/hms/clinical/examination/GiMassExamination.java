package com.hms.clinical.examination;

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
public class GiMassExamination {

    @Column(name = "gi_mass_present")
    private Boolean massPresent;

    @Column(name = "gi_mass_location")
    private String location;

    @Column(name = "gi_mass_size_cm")
    private Double sizeCm;

    @Enumerated(EnumType.STRING)
    @Column(name = "gi_mass_mobility", length = 10)
    private MassMobility mobility;

    @Enumerated(EnumType.STRING)
    @Column(name = "gi_mass_consistency", length = 10)
    private MassConsistency consistency;
}
