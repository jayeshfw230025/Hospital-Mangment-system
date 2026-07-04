package com.hms.prescription.service;

import com.hms.prescription.domain.Drug;
import com.hms.prescription.dto.DrugResponse;
import com.hms.prescription.repository.DrugRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DrugService {

    private final DrugRepository drugRepository;

    public DrugService(DrugRepository drugRepository) {
        this.drugRepository = drugRepository;
    }

    @Transactional(readOnly = true)
    public List<DrugResponse> search(String query) {
        return drugRepository
                .findByGenericNameContainingIgnoreCaseOrBrandNameContainingIgnoreCaseOrderByGenericNameAsc(query, query)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private DrugResponse toResponse(Drug drug) {
        return new DrugResponse(
                drug.getId(), drug.getGenericName(), drug.getBrandName(), drug.getCategory(), drug.getUnit(),
                drug.getStrength(), drug.getRouteOfAdministration(), drug.getSchedule(), drug.getContraindications(),
                drug.getDrugInteractions(), drug.getPediatricDoseMgPerKg(), drug.getAdultDose(), drug.getGeriatricDose(),
                drug.getMaximumDailyDose(), drug.getNutritionInteraction(), drug.getSideEffects(), drug.isActive());
    }
}
