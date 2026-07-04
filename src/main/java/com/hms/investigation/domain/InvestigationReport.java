package com.hms.investigation.domain;

import com.hms.common.audit.Auditable;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
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
@Table(name = "investigation_reports")
public class InvestigationReport extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "patient_upid", nullable = false, length = 20)
    private String patientUpid;

    @Column(name = "investigation_type_code", nullable = false, length = 40)
    private String investigationTypeCode;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "investigation_report_parameters", joinColumns = @JoinColumn(name = "report_id"))
    private List<ResultParameter> resultParameters = new ArrayList<>();

    @Column(name = "report_file_key")
    private String reportFileKey;

    @Column(name = "report_file_name")
    private String reportFileName;

    @Column(name = "report_content_type")
    private String reportContentType;

    @Column(name = "notes", length = 500)
    private String notes;
}
