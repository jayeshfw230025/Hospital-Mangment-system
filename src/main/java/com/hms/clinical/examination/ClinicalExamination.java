package com.hms.clinical.examination;

import com.hms.common.audit.Auditable;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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
 * Shared by both OPD and IPD (single table, single ID space) because
 * PUT /api/v1/clinical/{id} updates either kind through one generic path -
 * a record's examinationContext plus visitId/admissionId distinguishes the two.
 * IPD-only fields (systemicExamination, abdominalGirthCm) stay null for OPD records.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "clinical_examinations")
public class ClinicalExamination extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "examination_context", nullable = false, length = 10)
    private ExaminationContext examinationContext;

    @Column(name = "visit_id")
    private Long visitId;

    @Column(name = "admission_id")
    private Long admissionId;

    @Column(name = "patient_upid", nullable = false, length = 20)
    private String patientUpid;

    @Embedded
    private AbdominalExamination abdominalExamination;

    @Embedded
    private DigitalRectalExamination digitalRectalExamination;

    @Embedded
    private JaundiceAssessment jaundiceAssessment;

    @Embedded
    private HerniaExamination herniaExamination;

    @Embedded
    private LymphNodeExamination lymphNodeExamination;

    @Embedded
    private GiMassExamination giMassExamination;

    @Embedded
    private AscitesAssessment ascitesAssessment;

    @Embedded
    private SystemicExamination systemicExamination;

    @Column(name = "abdominal_girth_cm")
    private Double abdominalGirthCm;
}
