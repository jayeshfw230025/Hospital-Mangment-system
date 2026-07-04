CREATE TABLE patient_history (
    id                              BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_upid                    VARCHAR(20)     NOT NULL,

    other_chronic_diseases          VARCHAR(500)    NULL,

    blood_transfusion_history       BOOLEAN         NULL,
    blood_transfusion_details       VARCHAR(500)    NULL,

    current_illness_duration_value  INT             NULL,
    current_illness_duration_unit   VARCHAR(10)     NULL,
    current_illness_onset_date      DATE            NULL,
    current_illness_progression     VARCHAR(15)     NULL,
    current_illness_notes           VARCHAR(1000)   NULL,

    smoking_status                  VARCHAR(10)     NULL,
    smoking_pack_years               DOUBLE         NULL,
    alcohol_frequency               VARCHAR(15)     NULL,
    alcohol_type                    VARCHAR(100)    NULL,
    alcohol_quantity                VARCHAR(100)    NULL,
    alcohol_history_years           INT             NULL,
    dietary_habit                   VARCHAR(20)     NULL,
    physical_activity               VARCHAR(15)     NULL,
    sleep_hours_per_night           DOUBLE          NULL,
    stress_level                    VARCHAR(10)     NULL,
    occupation_exposure_history     VARCHAR(500)    NULL,
    recent_travel_history           VARCHAR(500)    NULL,

    created_at                      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by                      VARCHAR(100)    NULL,
    updated_by                      VARCHAR(100)    NULL,

    CONSTRAINT uk_patient_history_patient_upid UNIQUE (patient_upid),
    CONSTRAINT fk_patient_history_patient FOREIGN KEY (patient_upid) REFERENCES patients (upid)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE patient_history_chronic_diseases (
    patient_history_id  BIGINT       NOT NULL,
    chronic_disease      VARCHAR(30) NOT NULL,
    CONSTRAINT fk_phcd_history FOREIGN KEY (patient_history_id) REFERENCES patient_history (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE patient_history_surgeries (
    patient_history_id  BIGINT          NOT NULL,
    surgery_name         VARCHAR(255)    NULL,
    surgery_date         DATE            NULL,
    notes                VARCHAR(500)    NULL,
    CONSTRAINT fk_phs_history FOREIGN KEY (patient_history_id) REFERENCES patient_history (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE patient_history_hospitalizations (
    patient_history_id  BIGINT          NOT NULL,
    reason               VARCHAR(500)    NULL,
    admission_date       DATE            NULL,
    discharge_date       DATE            NULL,
    hospital_name        VARCHAR(255)    NULL,
    CONSTRAINT fk_phh_history FOREIGN KEY (patient_history_id) REFERENCES patient_history (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE patient_history_medications (
    patient_history_id  BIGINT          NOT NULL,
    drug_name            VARCHAR(255)    NULL,
    dosage               VARCHAR(100)    NULL,
    frequency            VARCHAR(100)    NULL,
    CONSTRAINT fk_phm_history FOREIGN KEY (patient_history_id) REFERENCES patient_history (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE patient_history_allergies (
    patient_history_id  BIGINT          NOT NULL,
    allergen             VARCHAR(255)    NULL,
    reaction_type        VARCHAR(255)    NULL,
    severity             VARCHAR(15)     NULL,
    hard_stop            BOOLEAN         NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_pha_history FOREIGN KEY (patient_history_id) REFERENCES patient_history (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE patient_history_immunizations (
    patient_history_id  BIGINT          NOT NULL,
    vaccine_name         VARCHAR(255)    NULL,
    date_administered    DATE            NULL,
    CONSTRAINT fk_phi_history FOREIGN KEY (patient_history_id) REFERENCES patient_history (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE family_history (
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_upid                VARCHAR(20)     NOT NULL,

    peptic_ulcer_disease        BOOLEAN         NOT NULL DEFAULT FALSE,
    inflammatory_bowel_disease  BOOLEAN         NOT NULL DEFAULT FALSE,
    gi_malignancy               BOOLEAN         NOT NULL DEFAULT FALSE,
    gi_malignancy_type          VARCHAR(255)    NULL,
    diabetes_mellitus           BOOLEAN         NOT NULL DEFAULT FALSE,
    hypertension                BOOLEAN         NOT NULL DEFAULT FALSE,
    coronary_artery_disease     BOOLEAN         NOT NULL DEFAULT FALSE,
    others_description          VARCHAR(500)    NULL,

    created_at                  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by                  VARCHAR(100)    NULL,
    updated_by                  VARCHAR(100)    NULL,

    CONSTRAINT uk_family_history_patient_upid UNIQUE (patient_upid),
    CONSTRAINT fk_family_history_patient FOREIGN KEY (patient_upid) REFERENCES patients (upid)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
