package com.hms.prescription.domain;

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
@Table(name = "prescriptions")
public class Prescription extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_upid", nullable = false, length = 20)
    private String patientUpid;

    @Column(name = "visit_id")
    private Long visitId;

    @Column(name = "admission_id")
    private Long admissionId;

    @Column(name = "prescribed_date", nullable = false)
    private LocalDate prescribedDate;

    @Column(name = "doctor_name", nullable = false)
    private String doctorName;

    @Column(name = "digital_signature", nullable = false, length = 1000)
    private String digitalSignature;

    @Column(name = "signature_hash", nullable = false, length = 100)
    private String signatureHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "template_used", length = 30)
    private GastroTemplateType templateUsed;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "prescription_items", joinColumns = @JoinColumn(name = "prescription_id"))
    private List<PrescriptionItem> items = new ArrayList<>();
}
