package com.hms.ipd.progressnote.dto;

import com.hms.ipd.progressnote.domain.MedicationPlanStatus;

public record MedicationPlanItemDto(String drugName, MedicationPlanStatus planStatus, String notes) {
}
