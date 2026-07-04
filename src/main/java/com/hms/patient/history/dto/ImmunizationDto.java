package com.hms.patient.history.dto;

import java.time.LocalDate;

public record ImmunizationDto(String vaccineName, LocalDate dateAdministered) {
}
