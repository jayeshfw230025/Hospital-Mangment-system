package com.hms.prescription.service;

import com.hms.prescription.domain.Drug;
import com.hms.prescription.domain.GastroTemplateType;
import com.hms.prescription.dto.DrugResponse;
import com.hms.prescription.dto.TemplateResponse;
import com.hms.prescription.repository.DrugRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class TemplateService {

    private final DrugRepository drugRepository;

    public TemplateService(DrugRepository drugRepository) {
        this.drugRepository = drugRepository;
    }

    @Transactional(readOnly = true)
    public List<TemplateResponse> listTemplates() {
        return Arrays.stream(GastroTemplateType.values())
                .map(this::toResponse)
                .toList();
    }

    private TemplateResponse toResponse(GastroTemplateType templateType) {
        List<DrugResponse> suggestedDrugs = drugRepository.findByCategoryInAndActiveTrue(templateType.getCategories())
                .stream()
                .map(this::toDrugResponse)
                .toList();

        return new TemplateResponse(templateType.name(), templateType.getLabel(), templateType.getCategories(), suggestedDrugs);
    }

    private DrugResponse toDrugResponse(Drug drug) {
        return new DrugResponse(
                drug.getId(), drug.getGenericName(), drug.getBrandName(), drug.getCategory(), drug.getUnit(),
                drug.getStrength(), drug.getRouteOfAdministration(), drug.getSchedule(), drug.getContraindications(),
                drug.getDrugInteractions(), drug.getPediatricDoseMgPerKg(), drug.getAdultDose(), drug.getGeriatricDose(),
                drug.getMaximumDailyDose(), drug.getNutritionInteraction(), drug.getSideEffects(), drug.isActive());
    }
}
