package com.hms.patient.history.dto;

import com.hms.patient.history.domain.AllergySeverity;

public record AllergyDto(String allergen, String reactionType, AllergySeverity severity, boolean hardStop) {
}
