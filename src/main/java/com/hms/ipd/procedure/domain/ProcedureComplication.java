package com.hms.ipd.procedure.domain;

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

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "procedure_complications")
public class ProcedureComplication extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "procedure_id", nullable = false)
    private Long procedureId;

    @Column(name = "complication_description", nullable = false, length = 500)
    private String complicationDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", length = 15)
    private SeverityLevel severity;

    @Column(name = "reported_date", nullable = false)
    private LocalDate reportedDate;

    @Column(name = "reported_by_name")
    private String reportedByName;
}
