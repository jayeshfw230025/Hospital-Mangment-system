package com.hms.diagnosis.domain;

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

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "diagnoses")
public class Diagnosis extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_upid", nullable = false, length = 20)
    private String patientUpid;

    @Column(name = "icd10_code", nullable = false, length = 10)
    private String icd10Code;

    @Enumerated(EnumType.STRING)
    @Column(name = "diagnosis_type", nullable = false, length = 15)
    private DiagnosisType diagnosisType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private DiagnosisStatus status;

    @Column(name = "diagnosed_date", nullable = false)
    private LocalDate diagnosedDate;

    @Column(name = "notes", length = 500)
    private String notes;
}
