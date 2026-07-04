CREATE TABLE icd10_codes (
    code            VARCHAR(10)     NOT NULL PRIMARY KEY,
    description     VARCHAR(255)    NOT NULL,
    category        VARCHAR(40)     NOT NULL,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_icd10_codes_category ON icd10_codes (category);

INSERT INTO icd10_codes (code, description, category, is_active) VALUES
('K25.0', 'Gastric ulcer, acute with haemorrhage', 'PEPTIC_ULCER_DISEASE', TRUE),
('K25.9', 'Gastric ulcer, unspecified', 'PEPTIC_ULCER_DISEASE', TRUE),
('K26.0', 'Duodenal ulcer, acute with haemorrhage', 'PEPTIC_ULCER_DISEASE', TRUE),
('K26.9', 'Duodenal ulcer, unspecified', 'PEPTIC_ULCER_DISEASE', TRUE),
('K29.0', 'Acute gastritis', 'GASTRITIS', TRUE),
('K29.5', 'Chronic gastritis', 'GASTRITIS', TRUE),
('K29.7', 'Gastritis, unspecified', 'GASTRITIS', TRUE),
('K20.0', 'Gastro-oesophageal reflux disease', 'ESOPHAGEAL_DISORDER', TRUE),
('K22.0', 'Achalasia of cardia', 'ESOPHAGEAL_DISORDER', TRUE),
('K22.9', 'Disease of oesophagus, unspecified', 'ESOPHAGEAL_DISORDER', TRUE),
('K31.0', 'Acute dilatation of stomach', 'GASTRIC_DISORDER', TRUE),
('K35.0', 'Acute appendicitis with perforation', 'APPENDICITIS', TRUE),
('K35.9', 'Acute appendicitis, unspecified', 'APPENDICITIS', TRUE),
('K50.0', 'Crohn''s disease of small intestine', 'INFLAMMATORY_BOWEL_DISEASE', TRUE),
('K50.1', 'Crohn''s disease of large intestine', 'INFLAMMATORY_BOWEL_DISEASE', TRUE),
('K51.0', 'Ulcerative colitis (chronic)', 'INFLAMMATORY_BOWEL_DISEASE', TRUE),
('K51.9', 'Ulcerative colitis, unspecified', 'INFLAMMATORY_BOWEL_DISEASE', TRUE),
('K52.9', 'Non-infective gastroenteritis', 'GASTROENTERITIS', TRUE),
('K57.0', 'Diverticular disease of intestine', 'DIVERTICULAR_DISEASE', TRUE),
('K58.0', 'Irritable bowel syndrome', 'FUNCTIONAL_BOWEL_DISORDER', TRUE),
('K59.1', 'Functional diarrhoea', 'FUNCTIONAL_BOWEL_DISORDER', TRUE),
('K60.0', 'Anal fistula', 'ANORECTAL_DISORDER', TRUE),
('K60.1', 'Anal abscess', 'ANORECTAL_DISORDER', TRUE),
('K61.0', 'Rectal abscess', 'ANORECTAL_DISORDER', TRUE),
('K62.4', 'Stenosis of anus and rectum', 'ANORECTAL_DISORDER', TRUE);

CREATE TABLE diagnoses (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_upid        VARCHAR(20)     NOT NULL,
    icd10_code          VARCHAR(10)     NOT NULL,
    diagnosis_type      VARCHAR(15)     NOT NULL,
    status              VARCHAR(15)     NOT NULL,
    diagnosed_date      DATE            NOT NULL,
    notes               VARCHAR(500)    NULL,

    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by           VARCHAR(100)    NULL,
    updated_by           VARCHAR(100)    NULL,

    CONSTRAINT fk_diagnoses_patient FOREIGN KEY (patient_upid) REFERENCES patients (upid),
    CONSTRAINT fk_diagnoses_icd10 FOREIGN KEY (icd10_code) REFERENCES icd10_codes (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_diagnoses_patient_upid ON diagnoses (patient_upid);
CREATE INDEX idx_diagnoses_icd10_code ON diagnoses (icd10_code);
