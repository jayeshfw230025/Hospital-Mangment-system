package com.hms.prescription.dto;

import com.hms.prescription.domain.DrugCategory;

import java.util.List;
import java.util.Set;

public record TemplateResponse(
        String name,
        String label,
        Set<DrugCategory> categories,
        List<DrugResponse> suggestedDrugs
) {
}
