package com.hms.diagnosis.domain;

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
 * Pre-configured GI ICD-10 master/reference data. code is the natural primary key
 * since it is inherently unique and meaningful; there is no CRUD API for this data,
 * only lookup/search, so it does not extend Auditable.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "icd10_codes")
public class Icd10Code {

    @Id
    @Column(name = "code", length = 10)
    private String code;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 40)
    private IcdCategory category;

    @Column(name = "is_active", nullable = false)
    private boolean active;
}
