package com.hms.prescription.service;

import com.hms.common.exception.AllergyHardStopException;
import com.hms.patient.domain.Patient;
import com.hms.patient.history.domain.Allergy;
import com.hms.patient.history.domain.CurrentMedication;
import com.hms.patient.history.domain.PatientHistory;
import com.hms.patient.history.repository.PatientHistoryRepository;
import com.hms.patient.repository.PatientRepository;
import com.hms.prescription.domain.Drug;
import com.hms.prescription.dto.DrugInteractionWarning;
import com.hms.prescription.dto.NutritionAlert;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Cross-cutting prescribing safety checks. Deliberately reaches into the Patient
 * History module (Module 3) - the Allergy.hardStop flag defined there exists
 * specifically so this module can enforce it here.
 */
@Service
public class PrescriptionSafetyService {

    private static final int PEDIATRIC_AGE_THRESHOLD_YEARS = 12;

    private final PatientHistoryRepository patientHistoryRepository;
    private final PatientRepository patientRepository;

    public PrescriptionSafetyService(PatientHistoryRepository patientHistoryRepository,
                                      PatientRepository patientRepository) {
        this.patientHistoryRepository = patientHistoryRepository;
        this.patientRepository = patientRepository;
    }

    public void enforceAllergyHardStop(String patientUpid, List<Drug> drugs) {
        Optional<PatientHistory> history = patientHistoryRepository.findByPatientUpid(patientUpid);
        if (history.isEmpty()) {
            return;
        }

        for (Allergy allergy : history.get().getAllergies()) {
            if (!allergy.isHardStop() || allergy.getAllergen() == null) {
                continue;
            }
            String allergen = allergy.getAllergen().toLowerCase();

            for (Drug drug : drugs) {
                if (namesMatch(allergen, drug.getGenericName()) || namesMatch(allergen, drug.getBrandName())) {
                    throw new AllergyHardStopException("Cannot prescribe " + drug.getGenericName()
                            + ": patient has a hard-stop allergy to " + allergy.getAllergen());
                }
            }
        }
    }

    public List<DrugInteractionWarning> checkInteractions(String patientUpid, List<Drug> drugs) {
        List<DrugInteractionWarning> warnings = new ArrayList<>(checkInteractionsAmongDrugs(drugs));

        if (patientUpid != null) {
            warnings.addAll(checkInteractionsAgainstCurrentMedications(patientUpid, drugs));
        }

        return warnings;
    }

    public List<NutritionAlert> getNutritionAlerts(List<Drug> drugs) {
        return drugs.stream()
                .filter(d -> d.getNutritionInteraction() != null && !d.getNutritionInteraction().isBlank())
                .map(d -> new NutritionAlert(d.getGenericName(), d.getNutritionInteraction()))
                .toList();
    }

    public Double calculatePediatricDoseMg(String patientUpid, Drug drug, Double patientWeightKg) {
        if (drug.getPediatricDoseMgPerKg() == null || patientWeightKg == null) {
            return null;
        }
        Patient patient = patientRepository.findByUpid(patientUpid).orElse(null);
        if (patient == null || patient.getDateOfBirth() == null) {
            return null;
        }
        int ageYears = Period.between(patient.getDateOfBirth(), LocalDate.now()).getYears();
        if (ageYears >= PEDIATRIC_AGE_THRESHOLD_YEARS) {
            return null;
        }
        return Math.round(drug.getPediatricDoseMgPerKg() * patientWeightKg * 100.0) / 100.0;
    }

    private List<DrugInteractionWarning> checkInteractionsAmongDrugs(List<Drug> drugs) {
        List<DrugInteractionWarning> warnings = new ArrayList<>();
        for (int i = 0; i < drugs.size(); i++) {
            for (int j = i + 1; j < drugs.size(); j++) {
                Drug a = drugs.get(i);
                Drug b = drugs.get(j);
                if (interactionListMentions(a.getDrugInteractions(), b.getGenericName())
                        || interactionListMentions(b.getDrugInteractions(), a.getGenericName())) {
                    warnings.add(new DrugInteractionWarning(a.getGenericName(), b.getGenericName(),
                            a.getGenericName() + " and " + b.getGenericName() + " have a known drug interaction"));
                }
            }
        }
        return warnings;
    }

    private List<DrugInteractionWarning> checkInteractionsAgainstCurrentMedications(String patientUpid, List<Drug> newDrugs) {
        Optional<PatientHistory> history = patientHistoryRepository.findByPatientUpid(patientUpid);
        if (history.isEmpty()) {
            return List.of();
        }

        List<DrugInteractionWarning> warnings = new ArrayList<>();
        for (CurrentMedication medication : history.get().getCurrentMedications()) {
            if (medication.getDrugName() == null) {
                continue;
            }
            for (Drug newDrug : newDrugs) {
                if (interactionListMentions(newDrug.getDrugInteractions(), medication.getDrugName())) {
                    warnings.add(new DrugInteractionWarning(newDrug.getGenericName(), medication.getDrugName(),
                            newDrug.getGenericName() + " may interact with the patient's current medication "
                                    + medication.getDrugName()));
                }
            }
        }
        return warnings;
    }

    private boolean interactionListMentions(String interactionList, String drugName) {
        if (interactionList == null || interactionList.isBlank() || drugName == null) {
            return false;
        }
        Set<String> interacting = Arrays.stream(interactionList.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        return interacting.stream().anyMatch(drugName.toLowerCase()::contains)
                || interacting.contains(drugName.toLowerCase());
    }

    private boolean namesMatch(String allergen, String drugName) {
        if (drugName == null || drugName.isBlank()) {
            return false;
        }
        String lowerDrugName = drugName.toLowerCase();
        return lowerDrugName.contains(allergen) || allergen.contains(lowerDrugName);
    }
}
