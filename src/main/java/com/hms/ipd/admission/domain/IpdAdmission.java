package com.hms.ipd.admission.domain;

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
@Table(name = "ipd_admissions")
public class IpdAdmission extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_upid", nullable = false, length = 20)
    private String patientUpid;

    @Column(name = "admission_date_time", nullable = false)
    private Instant admissionDateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "admission_type", nullable = false, length = 15)
    private AdmissionType admissionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "admission_source", nullable = false, length = 15)
    private AdmissionSource admissionSource;

    @Column(name = "referral_doctor_name")
    private String referralDoctorName;

    @Column(name = "referral_doctor_contact")
    private String referralDoctorContact;

    @Column(name = "referring_hospital_name")
    private String referringHospitalName;

    @Column(name = "referring_hospital_contact")
    private String referringHospitalContact;

    @Column(name = "primary_diagnosis_icd10", nullable = false, length = 10)
    private String primaryDiagnosisIcd10;

    @Column(name = "secondary_diagnosis_icd10", length = 10)
    private String secondaryDiagnosisIcd10;

    @Column(name = "clinical_summary", length = 2000)
    private String clinicalSummary;

    @Column(name = "consent_signature", length = 1000)
    private String consentSignature;

    @Column(name = "consent_document_file_key")
    private String consentDocumentFileKey;

    @Column(name = "consent_document_file_name")
    private String consentDocumentFileName;

    @Column(name = "bed_id")
    private Long bedId;
}
