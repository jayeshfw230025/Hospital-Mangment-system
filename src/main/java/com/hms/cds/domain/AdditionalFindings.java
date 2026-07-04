package com.hms.cds.domain;

/**
 * Clinical findings referenced by the alert rules that have no dedicated field
 * anywhere else in the system (Modules 1-8) - supplied by the assessing clinician
 * at the time of the CDS check rather than pulled automatically from the chart.
 */
public record AdditionalFindings(
        boolean darkUrine,
        boolean dehydration,
        boolean obstructionSymptoms,
        boolean spiderNevi,
        boolean hepaticEncephalopathySigns
) {
    public static AdditionalFindings none() {
        return new AdditionalFindings(false, false, false, false, false);
    }
}
