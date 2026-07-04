package com.hms.ipd.procedure.domain;

import java.util.Set;

/**
 * The 11 IPD procedure types from Module 12. requiredDetailKeys lists the most
 * clinically essential keys that must be present in a procedure's JSON "details"
 * map for that type - the remaining spec'd sub-fields (biopsy site, complications,
 * etc.) are recognized but optional, following the same pattern as Module 2's
 * ComplaintType.
 */
public enum ProcedureType {

    OGD("OGD (Upper GI Endoscopy)", Set.of("findings")),
    COLONOSCOPY("Colonoscopy", Set.of("bowelPreparationQuality", "findings", "cecalIntubation")),
    ERCP("ERCP", Set.of("cannulationSuccess", "findings")),
    EUS("EUS (Endoscopic Ultrasound)", Set.of("scope", "findings")),
    LIVER_BIOPSY("Liver Biopsy", Set.of("approach", "site", "needleSize", "numberOfPasses")),
    TIPS("TIPS", Set.of("reason", "stentType", "stentSizeMm")),
    ASCITIC_TAPPING("Ascitic Tapping", Set.of("site", "fluidVolumeMl")),
    PARACENTESIS("Paracentesis", Set.of("site", "volumeMl")),
    LAPAROSCOPY("Laparoscopy", Set.of("surgeryType", "findings")),
    CAPSULE_ENDOSCOPY("Capsule Endoscopy", Set.of("capsuleType", "findings")),
    DOUBLE_BALLOON_ENTEROSCOPY("Double Balloon Enteroscopy", Set.of("approach", "findings"));

    private final String label;
    private final Set<String> requiredDetailKeys;

    ProcedureType(String label, Set<String> requiredDetailKeys) {
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
