CREATE TABLE beds (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    ward_type               VARCHAR(20)     NOT NULL,
    room_number             VARCHAR(20)     NOT NULL,
    bed_number              VARCHAR(20)     NOT NULL,
    status                  VARCHAR(15)     NOT NULL DEFAULT 'AVAILABLE',
    current_admission_id    BIGINT          NULL,

    CONSTRAINT uk_beds_room_bed UNIQUE (room_number, bed_number)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_beds_status ON beds (status);
CREATE INDEX idx_beds_ward_type ON beds (ward_type);

INSERT INTO beds (ward_type, room_number, bed_number, status) VALUES
('GENERAL', 'G-101', 'A', 'AVAILABLE'), ('GENERAL', 'G-101', 'B', 'AVAILABLE'),
('GENERAL', 'G-102', 'A', 'AVAILABLE'), ('GENERAL', 'G-102', 'B', 'AVAILABLE'),
('GENERAL', 'G-103', 'A', 'AVAILABLE'), ('GENERAL', 'G-103', 'B', 'AVAILABLE'),
('GENERAL', 'G-104', 'A', 'AVAILABLE'), ('GENERAL', 'G-104', 'B', 'AVAILABLE'),
('GENERAL', 'G-105', 'A', 'AVAILABLE'), ('GENERAL', 'G-105', 'B', 'AVAILABLE'),
('ICU', 'ICU-201', '1', 'AVAILABLE'),
('ICU', 'ICU-202', '1', 'AVAILABLE'),
('ICU', 'ICU-203', '1', 'AVAILABLE'),
('ICU', 'ICU-204', '1', 'AVAILABLE'),
('ICU', 'ICU-205', '1', 'AVAILABLE'),
('PRIVATE', 'PVT-301', '1', 'AVAILABLE'),
('PRIVATE', 'PVT-302', '1', 'AVAILABLE'),
('PRIVATE', 'PVT-303', '1', 'AVAILABLE'),
('SEMI_PRIVATE', 'SP-401', 'A', 'AVAILABLE'), ('SEMI_PRIVATE', 'SP-401', 'B', 'AVAILABLE'),
('SEMI_PRIVATE', 'SP-402', 'A', 'AVAILABLE'), ('SEMI_PRIVATE', 'SP-402', 'B', 'AVAILABLE'),
('SEMI_PRIVATE', 'SP-403', 'A', 'AVAILABLE'), ('SEMI_PRIVATE', 'SP-403', 'B', 'AVAILABLE');

CREATE TABLE ipd_admissions (
    id                              BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_upid                    VARCHAR(20)     NOT NULL,
    admission_date_time             TIMESTAMP       NOT NULL,
    admission_type                  VARCHAR(15)     NOT NULL,
    admission_source                VARCHAR(15)     NOT NULL,
    referral_doctor_name            VARCHAR(255)    NULL,
    referral_doctor_contact         VARCHAR(50)     NULL,
    referring_hospital_name         VARCHAR(255)    NULL,
    referring_hospital_contact      VARCHAR(50)     NULL,
    primary_diagnosis_icd10         VARCHAR(10)     NOT NULL,
    secondary_diagnosis_icd10       VARCHAR(10)     NULL,
    clinical_summary                VARCHAR(2000)   NULL,
    consent_signature                VARCHAR(1000)   NULL,
    consent_document_file_key       VARCHAR(255)    NULL,
    consent_document_file_name      VARCHAR(255)    NULL,
    bed_id                          BIGINT          NULL,

    created_at                      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by                      VARCHAR(100)    NULL,
    updated_by                      VARCHAR(100)    NULL,

    CONSTRAINT fk_ipd_admissions_patient FOREIGN KEY (patient_upid) REFERENCES patients (upid),
    CONSTRAINT fk_ipd_admissions_primary_icd10 FOREIGN KEY (primary_diagnosis_icd10) REFERENCES icd10_codes (code),
    CONSTRAINT fk_ipd_admissions_secondary_icd10 FOREIGN KEY (secondary_diagnosis_icd10) REFERENCES icd10_codes (code),
    CONSTRAINT fk_ipd_admissions_bed FOREIGN KEY (bed_id) REFERENCES beds (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_ipd_admissions_patient_upid ON ipd_admissions (patient_upid);

CREATE TABLE bed_transfer_history (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    admission_id        BIGINT          NOT NULL,
    from_bed_id         BIGINT          NULL,
    to_bed_id           BIGINT          NOT NULL,
    reason              VARCHAR(500)    NULL,

    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by           VARCHAR(100)    NULL,
    updated_by           VARCHAR(100)    NULL,

    CONSTRAINT fk_bed_transfer_history_admission FOREIGN KEY (admission_id) REFERENCES ipd_admissions (id),
    CONSTRAINT fk_bed_transfer_history_from_bed FOREIGN KEY (from_bed_id) REFERENCES beds (id),
    CONSTRAINT fk_bed_transfer_history_to_bed FOREIGN KEY (to_bed_id) REFERENCES beds (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_bed_transfer_history_admission_id ON bed_transfer_history (admission_id);

CREATE TABLE tpa_pre_authorizations (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    admission_id            BIGINT          NOT NULL,
    insurance_company_name  VARCHAR(255)    NOT NULL,
    policy_number           VARCHAR(100)    NOT NULL,
    pre_auth_number         VARCHAR(100)    NULL,
    pre_auth_date           DATE            NULL,
    approval_status         VARCHAR(15)     NOT NULL,
    estimated_cost          DECIMAL(12, 2)  NULL,

    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by               VARCHAR(100)    NULL,
    updated_by               VARCHAR(100)    NULL,

    CONSTRAINT fk_tpa_pre_authorizations_admission FOREIGN KEY (admission_id) REFERENCES ipd_admissions (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_tpa_pre_authorizations_admission_id ON tpa_pre_authorizations (admission_id);
