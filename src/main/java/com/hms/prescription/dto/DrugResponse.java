package com.hms.prescription.dto;

import com.hms.prescription.domain.DrugCategory;
import com.hms.prescription.domain.DrugSchedule;
import com.hms.prescription.domain.DrugUnit;

public record DrugResponse(
        Long id,
        String genericName,
        String brandName,
        DrugCategory category,
        DrugUnit unit,
        String strength,
        String routeOfAdministration,
        DrugSchedule schedule,
        String contraindications,
        String drugInteractions,
        Double pediatricDoseMgPerKg,
        String adultDose,
        String geriatricDose,
        String maximumDailyDose,
        String nutritionInteraction,
        String sideEffects,
        boolean active
) {
}
