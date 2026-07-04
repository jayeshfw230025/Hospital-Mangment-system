package com.hms.analytics.dto;

/**
 * successRate uses "no recorded complication" as a proxy for procedural success,
 * since Module 12 captures complications but not an explicit success/failure flag.
 */
public record ProcedureStatEntry(String procedureType, String label, long totalCount,
                                  long complicationCount, double complicationRatePercent, double successRatePercent) {
}
