package com.hms.clinical.complaint;

import java.util.Set;

/**
 * The 20 structured GI chief complaints plus a free-text catch-all, shared by
 * both the OPD and IPD bounded contexts. requiredDetailKeys lists the keys that
 * must be present in a complaint's JSON "details" map for that type - fields
 * already covered by common entity columns (severity, duration, frequency) are
 * not repeated here.
 */
public enum ComplaintType {

    ABDOMINAL_PAIN("Abdominal Pain", Set.of("location", "aggravatingFactors", "relievingFactors")),
    NAUSEA_VOMITING("Nausea & Vomiting", Set.of("character", "bloodPresent")),
    HEARTBURN_REFLUX("Heartburn/Acid Reflux", Set.of()),
    DYSPHAGIA("Dysphagia", Set.of("solidOrLiquid", "progressivePattern")),
    EARLY_SATIETY("Early Satiety", Set.of("associatedSymptoms")),
    BLOATING("Bloating", Set.of()),
    DIARRHEA("Diarrhea", Set.of("character", "bloodOrMucus")),
    CONSTIPATION("Constipation", Set.of("hardStools")),
    BLOOD_IN_STOOL("Blood in Stool", Set.of("color", "amount", "associatedSymptoms")),
    MELENA("Melena", Set.of("amount")),
    HEMATEMESIS("Hematemesis", Set.of("amount")),
    JAUNDICE("Jaundice", Set.of("pruritus")),
    WEIGHT_LOSS("Weight Loss", Set.of("amount")),
    APPETITE_CHANGES("Appetite Changes", Set.of("direction")),
    ABDOMINAL_DISTENSION("Abdominal Distension", Set.of("onset", "progression")),
    RECTAL_BLEEDING("Rectal Bleeding", Set.of("amount")),
    ANOREXIA("Anorexia", Set.of()),
    FEVER("Fever", Set.of("temperature", "pattern")),
    FATIGUE("Fatigue", Set.of("dailyActivityImpact")),
    OTHERS("Others", Set.of("freeText"));

    private final String label;
    private final Set<String> requiredDetailKeys;

    ComplaintType(String label, Set<String> requiredDetailKeys) {
        this.label = label;
        this.requiredDetailKeys = requiredDetailKeys;
    }

    public String getLabel() {
        return label;
    }

    public Set<String> getRequiredDetailKeys() {
        return requiredDetailKeys;
    }
}
