package com.hms.patient.history.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Immunization {

    @Column(name = "vaccine_name")
    private String vaccineName;

    @Column(name = "date_administered")
    private LocalDate dateAdministered;
}
