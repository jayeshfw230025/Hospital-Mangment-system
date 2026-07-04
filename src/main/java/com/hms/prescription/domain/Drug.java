package com.hms.prescription.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Drug master/reference data. Not mutated via a CRUD API (only searched), so it
 * does not extend Auditable. drugInteractions is a comma-separated list of generic
 * drug names this drug interacts with - a pragmatic stand-in for a full drug
 * interaction graph, sufficient for the cross-checking this module needs.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "drugs")
public class Drug {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "generic_name", nullable = false)
    private String genericName;

    @Column(name = "brand_name")
    private String brandName;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private DrugCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit", nullable = false, length = 15)
    private DrugUnit unit;

    @Column(name = "strength")
    private String strength;

    @Column(name = "route_of_administration")
    private String routeOfAdministration;

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule", length = 5)
    private DrugSchedule schedule;

    @Column(name = "contraindications", length = 500)
    private String contraindications;

    @Column(name = "drug_interactions", length = 500)
    private String drugInteractions;

    @Column(name = "pediatric_dose_mg_per_kg")
    private Double pediatricDoseMgPerKg;

    @Column(name = "adult_dose")
    private String adultDose;

    @Column(name = "geriatric_dose")
    private String geriatricDose;

    @Column(name = "maximum_daily_dose")
    private String maximumDailyDose;

    @Column(name = "nutrition_interaction", length = 500)
    private String nutritionInteraction;

    @Column(name = "side_effects", length = 500)
    private String sideEffects;

    @Column(name = "is_active", nullable = false)
    private boolean active;
}
