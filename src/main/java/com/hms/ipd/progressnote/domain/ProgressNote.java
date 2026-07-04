package com.hms.ipd.progressnote.domain;

import com.hms.clinical.complaint.SeverityLevel;
import com.hms.common.audit.Auditable;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "progress_notes")
public class ProgressNote extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_id", nullable = false)
    private Long admissionId;

    @Column(name = "note_date", nullable = false)
    private LocalDate noteDate;

    // --- Subjective ---
    @Column(name = "chief_complaint_today")
    private String chiefComplaintToday;

    @Column(name = "pain_score")
    private Integer painScore;

    @Column(name = "nausea_vomiting")
    private Boolean nauseaVomiting;

    @Enumerated(EnumType.STRING)
    @Column(name = "appetite", length = 15)
    private AppetiteLevel appetite;

    @Column(name = "bowel_movement_frequency")
    private String bowelMovementFrequency;

    @Column(name = "bowel_movement_character")
    private String bowelMovementCharacter;

    @Column(name = "sleep_pattern")
    private String sleepPattern;

    @Column(name = "general_well_being")
    private String generalWellBeing;

    // --- Objective ---
    @Column(name = "ipd_vitals_id")
    private Long ipdVitalsId;

    @Column(name = "general_appearance")
    private String generalAppearance;

    @Column(name = "abdominal_examination_findings", length = 1000)
    private String abdominalExaminationFindings;

    @Column(name = "new_findings", length = 1000)
    private String newFindings;

    // --- Assessment ---
    @Column(name = "clinical_impression", length = 1000)
    private String clinicalImpression;

    @Column(name = "current_diagnosis")
    private String currentDiagnosis;

    @Column(name = "icd10_code", length = 10)
    private String icd10Code;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity_assessment", length = 15)
    private SeverityLevel severityAssessment;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "progress_note_complications", joinColumns = @JoinColumn(name = "progress_note_id"))
    @Column(name = "complication")
    private List<String> complicationFlags = new ArrayList<>();

    // --- Plan ---
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "progress_note_medication_plan", joinColumns = @JoinColumn(name = "progress_note_id"))
    private List<MedicationPlanItem> medicationPlanItems = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "progress_note_investigations", joinColumns = @JoinColumn(name = "progress_note_id"))
    @Column(name = "investigation")
    private List<String> investigationsOrdered = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "progress_note_consultations", joinColumns = @JoinColumn(name = "progress_note_id"))
    @Column(name = "consultation")
    private List<String> consultationsRequired = new ArrayList<>();

    @Column(name = "diet_plan")
    private String dietPlan;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_level", length = 25)
    private ActivityLevel activityLevel;

    @Column(name = "discharge_planning_notes", length = 1000)
    private String dischargePlanningNotes;
}
