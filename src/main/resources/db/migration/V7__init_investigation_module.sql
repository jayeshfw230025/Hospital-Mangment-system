CREATE TABLE investigation_types (
    code            VARCHAR(40)     NOT NULL PRIMARY KEY,
    name            VARCHAR(255)    NOT NULL,
    category        VARCHAR(15)     NOT NULL,
    ipd_only        BOOLEAN         NOT NULL DEFAULT FALSE,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_investigation_types_category ON investigation_types (category);

-- OPD lab investigations (1-15)
INSERT INTO investigation_types (code, name, category, ipd_only, is_active) VALUES
('CBC', 'Complete Blood Count (CBC)', 'LAB', FALSE, TRUE),
('ESR', 'ESR', 'LAB', FALSE, TRUE),
('CRP', 'C-Reactive Protein', 'LAB', FALSE, TRUE),
('LFT', 'Liver Function Test (LFT)', 'LAB', FALSE, TRUE),
('RFT', 'Renal Function Test (RFT)', 'LAB', FALSE, TRUE),
('SERUM_ELECTROLYTES', 'Serum Electrolytes', 'LAB', FALSE, TRUE),
('BLOOD_GLUCOSE', 'Blood Glucose (Fasting/Postprandial)', 'LAB', FALSE, TRUE),
('HBA1C', 'HbA1c', 'LAB', FALSE, TRUE),
('SERUM_AMYLASE', 'Serum Amylase', 'LAB', FALSE, TRUE),
('SERUM_LIPASE', 'Serum Lipase', 'LAB', FALSE, TRUE),
('H_PYLORI', 'H.pylori Antigen/Serology', 'LAB', FALSE, TRUE),
('STOOL_ANALYSIS', 'Stool Analysis (C/S)', 'LAB', FALSE, TRUE),
('URINE_ANALYSIS', 'Urine Analysis', 'LAB', FALSE, TRUE),
('ASCITIC_FLUID_ANALYSIS', 'Ascitic Fluid Analysis', 'LAB', FALSE, TRUE),
('PLEURAL_FLUID_ANALYSIS', 'Pleural Fluid Analysis', 'LAB', FALSE, TRUE);

-- OPD imaging/radiology and endoscopic procedures (16-25)
INSERT INTO investigation_types (code, name, category, ipd_only, is_active) VALUES
('USG_ABDOMEN', 'USG Abdomen', 'IMAGING', FALSE, TRUE),
('CT_ABDOMEN', 'CT Abdomen', 'IMAGING', FALSE, TRUE),
('MRI_ABDOMEN', 'MRI Abdomen', 'IMAGING', FALSE, TRUE),
('MRCP', 'MRCP', 'IMAGING', FALSE, TRUE),
('ERCP', 'ERCP', 'PROCEDURE', FALSE, TRUE),
('OGD', 'OGD (Upper GI Endoscopy)', 'PROCEDURE', FALSE, TRUE),
('COLONOSCOPY', 'Colonoscopy', 'PROCEDURE', FALSE, TRUE),
('LIVER_FIBROSCAN', 'Liver Fibroscan', 'IMAGING', FALSE, TRUE),
('BARIUM_STUDIES', 'Barium Studies', 'IMAGING', FALSE, TRUE),
('PET_CT', 'PET-CT', 'IMAGING', FALSE, TRUE);

-- IPD-only additional labs
INSERT INTO investigation_types (code, name, category, ipd_only, is_active) VALUES
('ABG', 'ABG (Arterial Blood Gas)', 'LAB', TRUE, TRUE),
('SERUM_AMMONIA', 'Serum Ammonia', 'LAB', TRUE, TRUE),
('PROCALCITONIN', 'Procalcitonin', 'LAB', TRUE, TRUE),
('D_DIMER', 'D-Dimer', 'LAB', TRUE, TRUE),
('BLOOD_CULTURE', 'Blood Culture', 'LAB', TRUE, TRUE),
('SPUTUM_CULTURE', 'Sputum Culture', 'LAB', TRUE, TRUE);

-- IPD-only additional imaging and procedures
INSERT INTO investigation_types (code, name, category, ipd_only, is_active) VALUES
('CTPA', 'CTPA (CT Pulmonary Angiogram)', 'IMAGING', TRUE, TRUE),
('CTP', 'CTP (Computed Tomography Portography)', 'IMAGING', TRUE, TRUE),
('HVPG', 'Hepatic Venous Pressure Gradient (HVPG)', 'PROCEDURE', TRUE, TRUE),
('PARACENTESIS', 'Paracentesis', 'PROCEDURE', TRUE, TRUE),
('PERITONEAL_BIOPSY', 'Peritoneal Biopsy', 'PROCEDURE', TRUE, TRUE),
('LIVER_BIOPSY', 'Liver Biopsy', 'PROCEDURE', TRUE, TRUE),
('EUS', 'Endoscopic Ultrasound (EUS)', 'PROCEDURE', TRUE, TRUE),
('CAPSULE_ENDOSCOPY', 'Capsule Endoscopy', 'PROCEDURE', TRUE, TRUE),
('DOUBLE_BALLOON_ENTEROSCOPY', 'Double Balloon Enteroscopy', 'PROCEDURE', TRUE, TRUE),
('INTRAOP_CHOLANGIOGRAPHY', 'Intraoperative Cholangiography', 'PROCEDURE', TRUE, TRUE);

CREATE TABLE investigation_orders (
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_upid                VARCHAR(20)     NOT NULL,
    visit_id                    BIGINT          NULL,
    admission_id                BIGINT          NULL,
    investigation_type_code     VARCHAR(40)     NOT NULL,
    ordered_date                DATE            NOT NULL,
    status                      VARCHAR(15)     NOT NULL,
    notes                       VARCHAR(500)    NULL,

    created_at                  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by                   VARCHAR(100)    NULL,
    updated_by                   VARCHAR(100)    NULL,

    CONSTRAINT fk_investigation_orders_patient FOREIGN KEY (patient_upid) REFERENCES patients (upid),
    CONSTRAINT fk_investigation_orders_type FOREIGN KEY (investigation_type_code) REFERENCES investigation_types (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_investigation_orders_patient_upid ON investigation_orders (patient_upid);
CREATE INDEX idx_investigation_orders_visit_id ON investigation_orders (visit_id);

CREATE TABLE investigation_reports (
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id                    BIGINT          NOT NULL,
    patient_upid                VARCHAR(20)     NOT NULL,
    investigation_type_code     VARCHAR(40)     NOT NULL,
    report_date                 DATE            NOT NULL,
    report_file_key             VARCHAR(255)    NULL,
    report_file_name            VARCHAR(255)    NULL,
    report_content_type         VARCHAR(100)    NULL,
    notes                       VARCHAR(500)    NULL,

    created_at                  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by                   VARCHAR(100)    NULL,
    updated_by                   VARCHAR(100)    NULL,

    CONSTRAINT fk_investigation_reports_order FOREIGN KEY (order_id) REFERENCES investigation_orders (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_investigation_reports_order_id ON investigation_reports (order_id);
CREATE INDEX idx_investigation_reports_patient_type ON investigation_reports (patient_upid, investigation_type_code);

CREATE TABLE investigation_report_parameters (
    report_id               BIGINT          NOT NULL,
    parameter_name          VARCHAR(255)    NULL,
    result_value            VARCHAR(255)    NULL,
    unit                     VARCHAR(50)     NULL,
    reference_range_low     DOUBLE          NULL,
    reference_range_high    DOUBLE          NULL,
    abnormal                 BOOLEAN         NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_investigation_report_parameters_report FOREIGN KEY (report_id) REFERENCES investigation_reports (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
