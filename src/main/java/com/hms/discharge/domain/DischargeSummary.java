package com.hms.discharge.domain;

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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "discharge_summaries")
public class DischargeSummary extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_id", nullable = false, unique = true)
    private Long admissionId;

    @Column(name = "discharge_date_time", nullable = false)
    private Instant dischargeDateTime;

    @Column(name = "length_of_stay_days")
    private Integer lengthOfStayDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "discharge_type", nullable = false, length = 15)
    private DischargeType dischargeType;

    @Column(name = "primary_diagnosis_icd10", nullable = false, length = 10)
    private String primaryDiagnosisIcd10;

    @Column(name = "secondary_diagnosis_icd10", length = 10)
    private String secondaryDiagnosisIcd10;

    @Column(name = "discharge_diagnosis_text", length = 1000)
    private String dischargeDiagnosisText;

    @Column(name = "summary_of_hospital_stay", length = 4000)
    private String summaryOfHospitalStay;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "discharge_significant_procedures", joinColumns = @JoinColumn(name = "discharge_summary_id"))
    @Column(name = "procedure_description")
    private List<String> significantProcedures = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "discharge_complications", joinColumns = @JoinColumn(name = "discharge_summary_id"))
    @Column(name = "complication_description")
    private List<String> complicationsDuringStay = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "discharge_medications", joinColumns = @JoinColumn(name = "discharge_summary_id"))
    private List<DischargeMedicationItem> dischargeMedications = new ArrayList<>();

    @Column(name = "discharge_diet_plan", length = 2000)
    private String dischargeDietPlan;

    @Column(name = "follow_up_date_time")
    private Instant followUpDateTime;

    @Column(name = "follow_up_instructions", length = 1000)
    private String followUpInstructions;

    @Enumerated(EnumType.STRING)
    @Column(name = "discharge_condition", length = 15)
    private DischargeCondition dischargeCondition;

    @Column(name = "discharged_by_doctor_name", nullable = false)
    private String dischargedByDoctorName;

    @Column(name = "discharged_by_doctor_signature", nullable = false, length = 1000)
    private String dischargedByDoctorSignature;

    @Column(name = "medical_records_checked")
    private Boolean medicalRecordsChecked;

    @Column(name = "discharge_instructions", length = 2000)
    private String dischargeInstructions;
}
