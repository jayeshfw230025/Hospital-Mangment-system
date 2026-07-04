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
public class PastSurgery {

    @Column(name = "surgery_name")
    private String surgeryName;

    @Column(name = "surgery_date")
    private LocalDate surgeryDate;

    @Column(name = "notes")
    private String notes;
}
