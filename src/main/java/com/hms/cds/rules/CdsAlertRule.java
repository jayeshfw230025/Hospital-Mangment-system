package com.hms.cds.rules;

import com.hms.cds.domain.CdsContext;
import com.hms.cds.domain.ClinicalSnapshot;
import com.hms.clinical.complaint.ComplaintType;

import java.util.function.Predicate;

/**
 * The 13 OPD + 8 IPD clinical decision support alert rules from Module 9. Each
 * constant pairs a human-readable finding/suggestion with the ClinicalSnapshot
 * condition that triggers it.
 */
public enum CdsAlertRule {

    JAUNDICE_DARK_URINE(CdsContext.OPD, "Jaundice + Dark Urine", "Suspect Hepatitis/Obstruction",
            s -> s.isJaundicePresent() && s.getAdditionalFindings().darkUrine()),

    HEMATEMESIS_HYPOTENSION(CdsContext.OPD, "Hematemesis + Hypotension", "GI Bleed Emergency",
            s -> s.getComplaints().contains(ComplaintType.HEMATEMESIS) && s.isHypotensive()),

    ABDOMINAL_PAIN_FEVER_RIGIDITY(CdsContext.OPD, "Abdominal Pain + Fever + Rigidity", "Surgical Abdomen",
            s -> s.getComplaints().contains(ComplaintType.ABDOMINAL_PAIN) && s.isFeverPresent() && s.isRigidityPresent()),

    WEIGHT_LOSS_ANEMIA(CdsContext.OPD, "Unexplained Weight Loss + Anemia", "Rule out Malignancy",
            s -> s.getComplaints().contains(ComplaintType.WEIGHT_LOSS) && s.isAnemiaPresent()),

    H_PYLORI_POSITIVE(CdsContext.OPD, "H.pylori Positive", "Consider Eradication Therapy",
            ClinicalSnapshot::isHPyloriPositive),

    LFT_ELEVATED(CdsContext.OPD, "LFT Elevated (>3x Normal)", "Assess for hepatitis/drug toxicity",
            ClinicalSnapshot::isLftElevatedOver3x),

    DIARRHEA_DEHYDRATION(CdsContext.OPD, "Diarrhea + Dehydration", "Fluid Replacement Needed",
            s -> s.getComplaints().contains(ComplaintType.DIARRHEA) && s.getAdditionalFindings().dehydration()),

    MELENA(CdsContext.OPD, "Black Stools (Melena)", "Upper GI Bleed",
            s -> s.getComplaints().contains(ComplaintType.MELENA)),

    DYSPHAGIA_WEIGHT_LOSS(CdsContext.OPD, "Dysphagia + Weight Loss", "Esophageal Malignancy",
            s -> s.getComplaints().contains(ComplaintType.DYSPHAGIA) && s.getComplaints().contains(ComplaintType.WEIGHT_LOSS)),

    MASS_OBSTRUCTION(CdsContext.OPD, "Abdominal Mass + Obstruction Symptoms", "Rule out Intestinal Obstruction",
            s -> s.isGiMassPresent() && s.getAdditionalFindings().obstructionSymptoms()),

    ASCITES_SPIDER_NEVI(CdsContext.OPD, "Ascites + Spider Nevi", "Suspect Chronic Liver Disease",
            s -> s.isAscitesPresent() && s.getAdditionalFindings().spiderNevi()),

    FAMILY_HISTORY_CRC_AGE(CdsContext.OPD, "Family History of CRC + Age >50", "Colorectal Cancer Screening",
            s -> s.isFamilyHistoryGiMalignancy() && s.getAgeYears() != null && s.getAgeYears() > 50),

    PAIN_JAUNDICE_FEVER(CdsContext.OPD, "Pain + Jaundice + Fever", "Cholangitis/Cholecystitis",
            s -> s.getComplaints().contains(ComplaintType.ABDOMINAL_PAIN) && s.isJaundicePresent() && s.isFeverPresent()),

    HEMODYNAMIC_INSTABILITY(CdsContext.IPD, "Hemodynamic Instability", "ICU Transfer Decision",
            s -> s.isHypotensive() || s.isTachycardic()),

    DECREASED_GCS(CdsContext.IPD, "Decreased GCS (≤12)", "Neurological Deterioration",
            s -> s.getGcsScore() != null && s.getGcsScore() <= 12),

    SEPTIC_SHOCK(CdsContext.IPD, "Septic Shock (Hypotension + Fever)", "Sepsis Protocol",
            s -> s.isHypotensive() && s.isFeverPresent()),

    HEPATIC_ENCEPHALOPATHY(CdsContext.IPD, "Hepatic Encephalopathy", "Start Lactulose/Rifaximin",
            s -> s.getAdditionalFindings().hepaticEncephalopathySigns()),

    AKI_RISING_CREATININE(CdsContext.IPD, "AKI (Rising Creatinine)", "Renal Failure Risk",
            ClinicalSnapshot::isCreatinineRising),

    GI_BLEED_HB_DROP(CdsContext.IPD, "GI Bleed (Hemoglobin Drop)", "Blood Transfusion Alert",
            ClinicalSnapshot::isHemoglobinDropping),

    RESPIRATORY_DISTRESS(CdsContext.IPD, "Respiratory Distress (SpO2 <94%)", "Oxygen/ICU Transfer",
            s -> s.getSpo2() != null && s.getSpo2() < 94),

    DIC_COAGULOPATHY(CdsContext.IPD, "DIC (Thrombocytopenia + Prolonged PT)", "Coagulopathy Alert",
            s -> s.isPlateletsLow() && s.isPtOrInrAbnormal());

    private final CdsContext context;
    private final String finding;
    private final String suggestion;
    private final Predicate<ClinicalSnapshot> condition;

    CdsAlertRule(CdsContext context, String finding, String suggestion, Predicate<ClinicalSnapshot> condition) {
        this.context = context;
        this.finding = finding;
        this.suggestion = suggestion;
        this.condition = condition;
    }

    public CdsContext getContext() {
        return context;
    }

    public String getFinding() {
        return finding;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public boolean matches(ClinicalSnapshot snapshot) {
        return condition.test(snapshot);
    }
}
