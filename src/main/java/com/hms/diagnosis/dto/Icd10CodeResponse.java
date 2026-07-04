package com.hms.diagnosis.dto;

import com.hms.diagnosis.domain.IcdCategory;

public record Icd10CodeResponse(String code, String description, IcdCategory category, boolean active) {
}
