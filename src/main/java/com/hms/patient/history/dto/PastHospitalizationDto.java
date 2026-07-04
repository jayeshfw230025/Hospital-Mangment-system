package com.hms.patient.history.dto;

import java.time.LocalDate;

public record PastHospitalizationDto(String reason, LocalDate admissionDate, LocalDate dischargeDate, String hospitalName) {
}
