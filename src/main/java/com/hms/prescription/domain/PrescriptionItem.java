package com.hms.prescription.domain;

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
public class PrescriptionItem {

    @Column(name = "drug_id", nullable = false)
    private Long drugId;

    @Column(name = "generic_name", nullable = false)
    private String genericName;

    @Column(name = "dosage", nullable = false)
    private String dosage;

    @Column(name = "frequency", nullable = false)
    private String frequency;

    @Column(name = "route")
    private String route;

    @Column(name = "duration_days")
    private Integer durationDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "food_instruction", length = 20)
    private FoodInstruction foodInstruction;

    @Column(name = "generated_instructions", length = 500)
    private String generatedInstructions;

    @Column(name = "refills_allowed")
    private Integer refillsAllowed;

    @Column(name = "refills_used")
    private Integer refillsUsed;

    @Column(name = "calculated_pediatric_dose_mg")
    private Double calculatedPediatricDoseMg;
}
