CREATE TABLE progress_notes (
    id                              BIGINT AUTO_INCREMENT PRIMARY KEY,
    admission_id                    BIGINT          NOT NULL,
    note_date                       DATE            NOT NULL,

    chief_complaint_today           VARCHAR(500)    NULL,
    pain_score                      INT             NULL,
    nausea_vomiting                 BOOLEAN         NULL,
    appetite                        VARCHAR(15)     NULL,
    bowel_movement_frequency        VARCHAR(100)    NULL,
    bowel_movement_character        VARCHAR(100)    NULL,
    sleep_pattern                   VARCHAR(255)    NULL,
    general_well_being              VARCHAR(255)    NULL,

    ipd_vitals_id                   BIGINT          NULL,
    general_appearance              VARCHAR(255)    NULL,
    abdominal_examination_findings  VARCHAR(1000)   NULL,
    new_findings                    VARCHAR(1000)   NULL,

    clinical_impression             VARCHAR(1000)   NULL,
    current_diagnosis               VARCHAR(255)    NULL,
    icd10_code                      VARCHAR(10)     NULL,
    severity_assessment             VARCHAR(15)     NULL,

    diet_plan                       VARCHAR(500)    NULL,
    activity_level                  VARCHAR(25)     NULL,
    discharge_planning_notes        VARCHAR(1000)   NULL,

    created_at                      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by                      VARCHAR(100)    NULL,
    updated_by                      VARCHAR(100)    NULL,

    CONSTRAINT fk_progress_notes_admission FOREIGN KEY (admission_id) REFERENCES ipd_admissions (id),
    CONSTRAINT fk_progress_notes_vitals FOREIGN KEY (ipd_vitals_id) REFERENCES ipd_vitals (id),
    CONSTRAINT fk_progress_notes_icd10 FOREIGN KEY (icd10_code) REFERENCES icd10_codes (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_progress_notes_admission_id ON progress_notes (admission_id);

CREATE TABLE progress_note_complications (
    progress_note_id    BIGINT          NOT NULL,
    complication         VARCHAR(255)    NULL,
    CONSTRAINT fk_pnc_note FOREIGN KEY (progress_note_id) REFERENCES progress_notes (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE progress_note_medication_plan (
    progress_note_id    BIGINT          NOT NULL,
    drug_name            VARCHAR(255)    NULL,
    plan_status          VARCHAR(15)     NULL,
    notes                VARCHAR(255)    NULL,
    CONSTRAINT fk_pnmp_note FOREIGN KEY (progress_note_id) REFERENCES progress_notes (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE progress_note_investigations (
    progress_note_id    BIGINT          NOT NULL,
    investigation        VARCHAR(255)    NULL,
    CONSTRAINT fk_pni_note FOREIGN KEY (progress_note_id) REFERENCES progress_notes (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE progress_note_consultations (
    progress_note_id    BIGINT          NOT NULL,
    consultation          VARCHAR(255)    NULL,
    CONSTRAINT fk_pncon_note FOREIGN KEY (progress_note_id) REFERENCES progress_notes (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE medication_administrations (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    admission_id            BIGINT          NOT NULL,
    drug_id                 BIGINT          NULL,
    drug_name               VARCHAR(255)    NOT NULL,
    dosage                  VARCHAR(100)    NULL,
    route                   VARCHAR(50)     NULL,
    scheduled_time          TIMESTAMP       NOT NULL,
    administered_time       TIMESTAMP       NULL,
    administered_by_name    VARCHAR(255)    NULL,
    status                  VARCHAR(15)     NOT NULL,
    notes                   VARCHAR(500)    NULL,

    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by               VARCHAR(100)    NULL,
    updated_by               VARCHAR(100)    NULL,

    CONSTRAINT fk_medication_administrations_admission FOREIGN KEY (admission_id) REFERENCES ipd_admissions (id),
    CONSTRAINT fk_medication_administrations_drug FOREIGN KEY (drug_id) REFERENCES drugs (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_medication_administrations_admission_id ON medication_administrations (admission_id);
