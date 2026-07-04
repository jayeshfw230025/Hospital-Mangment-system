package com.hms.ipd.progressnote.domain;

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
public class MedicationPlanItem {

    @Column(name = "drug_name")
    private String drugName;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_status", length = 15)
    private MedicationPlanStatus planStatus;

    @Column(name = "notes", length = 255)
    private String notes;
}
