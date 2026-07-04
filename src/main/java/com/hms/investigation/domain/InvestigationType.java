package com.hms.investigation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pre-configured investigation master/reference data (25 OPD + 16 IPD-only = 41
 * entries). code is the natural primary key; this is never mutated via API, only
 * validated against when creating orders, so it does not extend Auditable.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "investigation_types")
public class InvestigationType {

    @Id
    @Column(name = "code", length = 40)
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 15)
    private InvestigationCategory category;

    @Column(name = "ipd_only", nullable = false)
    private boolean ipdOnly;

    @Column(name = "is_active", nullable = false)
    private boolean active;
}
