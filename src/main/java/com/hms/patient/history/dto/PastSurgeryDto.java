package com.hms.patient.history.dto;

import java.time.LocalDate;

public record PastSurgeryDto(String surgeryName, LocalDate surgeryDate, String notes) {
}
