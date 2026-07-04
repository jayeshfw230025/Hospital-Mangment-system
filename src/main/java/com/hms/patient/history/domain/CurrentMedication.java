package com.hms.patient.history.domain;

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
public class CurrentMedication {

    @Column(name = "drug_name")
    private String drugName;

    @Column(name = "dosage")
    private String dosage;

    @Column(name = "frequency")
    private String frequency;
}
