package com.hms.opd.domain;

import com.hms.clinical.complaint.ComplaintType;
import com.hms.clinical.complaint.DurationUnit;
import com.hms.clinical.complaint.FrequencyLevel;
import com.hms.clinical.complaint.SeverityLevel;
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

import java.time.LocalDate;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "opd_complaints")
public class OpdComplaint extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "visit_id", nullable = false)
    private Long visitId;

    @Enumerated(EnumType.STRING)
    @Column(name = "complaint_type", nullable = false, length = 30)
    private ComplaintType complaintType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", length = 15)
    private SeverityLevel severity;

    @Column(name = "duration_value")
    private Integer durationValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "duration_unit", length = 10)
    private DurationUnit durationUnit;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", length = 15)
    private FrequencyLevel frequency;

    @Column(name = "onset_date")
    private LocalDate onsetDate;

    @Column(name = "notes", length = 1000)
    private String notes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details")
    private Map<String, Object> details;
}
