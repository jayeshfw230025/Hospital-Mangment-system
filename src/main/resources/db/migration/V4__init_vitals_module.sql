CREATE TABLE opd_vitals (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    visit_id                BIGINT          NOT NULL,
    patient_upid            VARCHAR(20)     NOT NULL,

    systolic_bp             INT             NULL,
    diastolic_bp            INT             NULL,
    heart_rate              INT             NULL,
    respiratory_rate        INT             NULL,
    temperature_celsius     DOUBLE          NULL,
    height_cm               DOUBLE          NULL,
    weight_kg               DOUBLE          NULL,
    bmi                     DOUBLE          NULL,
    spo2                    INT             NULL,
    pain_score              INT             NULL,
    random_blood_sugar      INT             NULL,

    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by               VARCHAR(100)    NULL,
    updated_by               VARCHAR(100)    NULL,

    CONSTRAINT fk_opd_vitals_patient FOREIGN KEY (patient_upid) REFERENCES patients (upid)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_opd_vitals_visit_id ON opd_vitals (visit_id);
CREATE INDEX idx_opd_vitals_patient_upid ON opd_vitals (patient_upid);

CREATE TABLE ipd_vitals (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    admission_id            BIGINT          NOT NULL,
    patient_upid            VARCHAR(20)     NOT NULL,

    systolic_bp             INT             NULL,
    diastolic_bp            INT             NULL,
    heart_rate              INT             NULL,
    respiratory_rate        INT             NULL,
    temperature_celsius     DOUBLE          NULL,
    height_cm               DOUBLE          NULL,
    weight_kg               DOUBLE          NULL,
    bmi                     DOUBLE          NULL,
    spo2                    INT             NULL,
    pain_score              INT             NULL,
    random_blood_sugar      INT             NULL,

    qtc_ms                  INT             NULL,
    map_value               DOUBLE          NULL,
    input_output_balance_ml INT             NULL,
    gcs_score               INT             NULL,
    cvp_cm_h2o              DOUBLE          NULL,
    gag_reflex              VARCHAR(10)     NULL,

    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by               VARCHAR(100)    NULL,
    updated_by               VARCHAR(100)    NULL,

    CONSTRAINT fk_ipd_vitals_patient FOREIGN KEY (patient_upid) REFERENCES patients (upid)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_ipd_vitals_admission_id ON ipd_vitals (admission_id);
CREATE INDEX idx_ipd_vitals_patient_upid ON ipd_vitals (patient_upid);

CREATE TABLE vital_alerts (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_upid        VARCHAR(20)     NOT NULL,
    source_type         VARCHAR(10)     NOT NULL,
    source_vitals_id    BIGINT          NOT NULL,
    parameter           VARCHAR(25)     NOT NULL,
    measured_value      VARCHAR(50)     NULL,
    message             VARCHAR(500)    NULL,
    acknowledged        BOOLEAN         NOT NULL DEFAULT FALSE,
    acknowledged_by     VARCHAR(100)    NULL,
    acknowledged_at     TIMESTAMP       NULL,

    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by           VARCHAR(100)    NULL,
    updated_by           VARCHAR(100)    NULL,

    CONSTRAINT fk_vital_alerts_patient FOREIGN KEY (patient_upid) REFERENCES patients (upid)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_vital_alerts_patient_upid ON vital_alerts (patient_upid);
CREATE INDEX idx_vital_alerts_source ON vital_alerts (source_type, source_vitals_id);
CREATE INDEX idx_vital_alerts_acknowledged ON vital_alerts (acknowledged);
