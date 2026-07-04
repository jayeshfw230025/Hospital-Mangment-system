CREATE TABLE patients (
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    upid                        VARCHAR(20)     NOT NULL,
    abha_number                 VARCHAR(20)     NULL,

    full_name                   VARCHAR(255)    NOT NULL,
    date_of_birth               DATE            NOT NULL,
    gender                      VARCHAR(10)     NOT NULL,
    marital_status              VARCHAR(15)     NULL,
    blood_group                 VARCHAR(15)     NULL,
    nationality                 VARCHAR(100)    NULL,
    religion                    VARCHAR(100)    NULL,
    occupation                  VARCHAR(100)    NULL,
    education                   VARCHAR(100)    NULL,

    primary_contact_number      VARCHAR(15)     NOT NULL,
    secondary_contact_number    VARCHAR(15)     NULL,
    email                       VARCHAR(255)    NULL,

    address_line1               VARCHAR(255)    NULL,
    address_line2               VARCHAR(255)    NULL,
    city                        VARCHAR(100)    NULL,
    state                       VARCHAR(100)    NULL,
    district                    VARCHAR(100)    NULL,
    pin_code                    VARCHAR(10)     NULL,
    country                     VARCHAR(100)    NULL DEFAULT 'India',

    latitude                    DECIMAL(10, 7)  NULL,
    longitude                   DECIMAL(10, 7)  NULL,

    aadhaar_number              VARCHAR(255)    NULL,
    govt_id_type                VARCHAR(20)     NULL,
    govt_id_number              VARCHAR(255)    NULL,

    emergency_contact_name      VARCHAR(255)    NULL,
    emergency_contact_number    VARCHAR(15)     NULL,
    emergency_contact_relation  VARCHAR(100)    NULL,

    referring_doctor_name       VARCHAR(255)    NULL,
    referring_hospital_name     VARCHAR(255)    NULL,
    referral_date               DATE            NULL,
    referral_reason             VARCHAR(500)    NULL,
    referral_contact_number     VARCHAR(15)     NULL,

    created_at                  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by                  VARCHAR(100)    NULL,
    updated_by                  VARCHAR(100)    NULL,

    CONSTRAINT uk_patients_upid UNIQUE (upid),
    CONSTRAINT uk_patients_primary_contact UNIQUE (primary_contact_number)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_patients_full_name ON patients (full_name);
CREATE INDEX idx_patients_dob ON patients (date_of_birth);
CREATE INDEX idx_patients_abha_number ON patients (abha_number);
