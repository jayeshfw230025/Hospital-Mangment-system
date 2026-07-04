package com.hms.ipd.mar.domain;

import com.hms.common.audit.Auditable;
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

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "medication_administrations")
public class MedicationAdministration extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_id", nullable = false)
    private Long admissionId;

    @Column(name = "drug_id")
    private Long drugId;

    @Column(name = "drug_name", nullable = false)
    private String drugName;

    @Column(name = "dosage")
    private String dosage;

    @Column(name = "route")
    private String route;

    @Column(name = "scheduled_time", nullable = false)
    private Instant scheduledTime;

    @Column(name = "administered_time")
    private Instant administeredTime;

    @Column(name = "administered_by_name")
    private String administeredByName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private AdministrationStatus status;

    @Column(name = "notes", length = 500)
    private String notes;
}
