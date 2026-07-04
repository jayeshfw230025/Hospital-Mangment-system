package com.hms.ipd.domain;

import com.hms.clinical.complaint.ComplaintType;
import com.hms.clinical.complaint.DurationUnit;
import com.hms.clinical.complaint.SeverityLevel;
import com.hms.clinical.complaint.TreatmentResponse;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ipd_complaints")
public class IpdComplaint extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_id", nullable = false)
    private Long admissionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "complaint_type", nullable = false, length = 30)
    private ComplaintType complaintType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", length = 15)
    private SeverityLevel severity;

    @Column(name = "severity_score")
    private Integer severityScore;

    @Column(name = "duration_value")
    private Integer durationValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "duration_unit", length = 10)
    private DurationUnit durationUnit;

    @Column(name = "associated_vitals_impact", length = 500)
    private String associatedVitalsImpact;

    @Enumerated(EnumType.STRING)
    @Column(name = "response_to_initial_treatment", length = 20)
    private TreatmentResponse responseToInitialTreatment;

    @Column(name = "notes", length = 1000)
    private String notes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details")
    private Map<String, Object> details;
}
