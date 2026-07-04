package com.hms.patient.history.domain;

import com.hms.common.audit.Auditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "family_history")
public class FamilyHistory extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_upid", nullable = false, unique = true, length = 20)
    private String patientUpid;

    @Column(name = "peptic_ulcer_disease")
    private boolean pepticUlcerDisease;

    @Column(name = "inflammatory_bowel_disease")
    private boolean inflammatoryBowelDisease;

    @Column(name = "gi_malignancy")
    private boolean giMalignancy;

    @Column(name = "gi_malignancy_type")
    private String giMalignancyType;

    @Column(name = "diabetes_mellitus")
    private boolean diabetesMellitus;

    @Column(name = "hypertension")
    private boolean hypertension;

    @Column(name = "coronary_artery_disease")
    private boolean coronaryArteryDisease;

    @Column(name = "others_description", length = 500)
    private String othersDescription;
}
