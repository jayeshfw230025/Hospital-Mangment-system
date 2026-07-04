package com.hms.nutrition.service;

import com.hms.common.exception.ResourceNotFoundException;
import com.hms.ipd.admission.repository.IpdAdmissionRepository;
import com.hms.nutrition.domain.NutritionAssessment;
import com.hms.nutrition.dto.NutritionAssessmentRequest;
import com.hms.nutrition.dto.NutritionAssessmentResponse;
import com.hms.nutrition.repository.NutritionAssessmentRepository;
import com.hms.patient.domain.Patient;
import com.hms.patient.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
public class NutritionAssessmentService {

    private final NutritionAssessmentRepository nutritionAssessmentRepository;
    private final PatientRepository patientRepository;
    private final IpdAdmissionRepository ipdAdmissionRepository;
    private final NutritionCalculatorService nutritionCalculatorService;

    public NutritionAssessmentService(NutritionAssessmentRepository nutritionAssessmentRepository,
                                       PatientRepository patientRepository,
                                       IpdAdmissionRepository ipdAdmissionRepository,
                                       NutritionCalculatorService nutritionCalculatorService) {
        this.nutritionAssessmentRepository = nutritionAssessmentRepository;
        this.patientRepository = patientRepository;
        this.ipdAdmissionRepository = ipdAdmissionRepository;
        this.nutritionCalculatorService = nutritionCalculatorService;
    }

    @Transactional
    public NutritionAssessmentResponse create(NutritionAssessmentRequest request) {
        Patient patient = patientRepository.findByUpid(request.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with UPID: " + request.patientId()));
        requireAdmissionExistsIfProvided(request.admissionId());

        NutritionAssessment assessment = new NutritionAssessment();
        assessment.setPatientUpid(request.patientId());
        assessment.setAssessmentDate(LocalDate.now());
        applyRequest(assessment, request, patient);

        return toResponse(nutritionAssessmentRepository.save(assessment), patient);
    }

    @Transactional
    public NutritionAssessmentResponse update(Long id, NutritionAssessmentRequest request) {
        NutritionAssessment assessment = nutritionAssessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nutrition assessment not found with id: " + id));

        Patient patient = patientRepository.findByUpid(request.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with UPID: " + request.patientId()));
        requireAdmissionExistsIfProvided(request.admissionId());

        assessment.setPatientUpid(request.patientId());
        applyRequest(assessment, request, patient);

        return toResponse(nutritionAssessmentRepository.save(assessment), patient);
    }

    @Transactional(readOnly = true)
    public List<NutritionAssessmentResponse> getByPatientId(String patientId) {
        Patient patient = patientRepository.findByUpid(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with UPID: " + patientId));

        return nutritionAssessmentRepository.findByPatientUpidOrderByCreatedAtDesc(patientId).stream()
                .map(a -> toResponse(a, patient))
                .toList();
    }

    private void applyRequest(NutritionAssessment assessment, NutritionAssessmentRequest request, Patient patient) {
        assessment.setAdmissionId(request.admissionId());
        assessment.setWeightKg(request.weightKg());
        assessment.setHeightCm(request.heightCm());
        assessment.setWeightLossPercent(request.weightLossPercent());
        assessment.setDietaryIntakePercent(request.dietaryIntakePercent());
        assessment.setDiseaseSeverityScore(request.diseaseSeverityScore() == null ? 0 : request.diseaseSeverityScore());
        assessment.setAcuteDiseaseEffect(Boolean.TRUE.equals(request.acuteDiseaseEffect()));
        assessment.setDiseaseCategory(request.diseaseCategory());
        assessment.setSodiumRestrictionMeqPerDay(request.sodiumRestrictionMeqPerDay());
        assessment.setPotassiumBalanceMeqPerDay(request.potassiumBalanceMeqPerDay());
        assessment.setEnteralParenteralSupport(request.enteralParenteralSupport());
        assessment.setDieticianAssessment(request.dieticianAssessment());
        assessment.setWeeklyFollowUpPlan(request.weeklyFollowUpPlan());

        Double bmi = computeBmi(request.heightCm(), request.weightKg());
        assessment.setBmi(bmi);

        Integer age = patient.getDateOfBirth() == null ? null
                : Period.between(patient.getDateOfBirth(), LocalDate.now()).getYears();

        NrsResult nrs = nutritionCalculatorService.calculateNrs(
                request.weightLossPercent(), bmi, request.dietaryIntakePercent(), assessment.getDiseaseSeverityScore(), age);
        assessment.setNrsNutritionalStatusScore(nrs.nutritionalStatusScore());
        assessment.setNrsAgeAdjustment(nrs.ageAdjustment());
        assessment.setNrsTotalScore(nrs.totalScore());
        assessment.setNrsAtRisk(nrs.atRisk());

        MustResult must = nutritionCalculatorService.calculateMust(
                bmi, request.weightLossPercent(), assessment.getAcuteDiseaseEffect());
        assessment.setMustBmiScore(must.bmiScore());
        assessment.setMustWeightLossScore(must.weightLossScore());
        assessment.setMustAcuteDiseaseScore(must.acuteDiseaseScore());
        assessment.setMustTotalScore(must.totalScore());
        assessment.setMustRiskCategory(must.riskCategory());

        if (request.diseaseCategory() != null && request.weightKg() != null) {
            NutritionTargets targets = nutritionCalculatorService.calculateTargets(request.diseaseCategory(), request.weightKg());
            assessment.setCaloricTargetMinKcalPerDay(targets.caloricTargetMinKcalPerDay());
            assessment.setCaloricTargetMaxKcalPerDay(targets.caloricTargetMaxKcalPerDay());
            assessment.setProteinTargetMinGPerDay(targets.proteinTargetMinGPerDay());
            assessment.setProteinTargetMaxGPerDay(targets.proteinTargetMaxGPerDay());
            assessment.setFluidRequirementMlPerDay(request.fluidRequirementMlPerDayOverride() == null
                    ? targets.fluidRequirementMlPerDay() : request.fluidRequirementMlPerDayOverride());
        } else {
            assessment.setFluidRequirementMlPerDay(request.fluidRequirementMlPerDayOverride());
        }
    }

    private Double computeBmi(Double heightCm, Double weightKg) {
        if (heightCm == null || weightKg == null || heightCm <= 0) {
            return null;
        }
        double heightM = heightCm / 100.0;
        return Math.round((weightKg / (heightM * heightM)) * 100.0) / 100.0;
    }

    private void requireAdmissionExistsIfProvided(Long admissionId) {
        if (admissionId != null && ipdAdmissionRepository.findById(admissionId).isEmpty()) {
            throw new ResourceNotFoundException("Admission not found with id: " + admissionId);
        }
    }

    private NutritionAssessmentResponse toResponse(NutritionAssessment a, Patient patient) {
        Integer age = patient.getDateOfBirth() == null ? null
                : Period.between(patient.getDateOfBirth(), LocalDate.now()).getYears();

        return new NutritionAssessmentResponse(
                a.getId(), a.getPatientUpid(), a.getAdmissionId(), a.getAssessmentDate(),
                a.getWeightKg(), a.getHeightCm(), a.getBmi(), age,
                a.getWeightLossPercent(), a.getDietaryIntakePercent(), a.getDiseaseSeverityScore(),
                a.getNrsNutritionalStatusScore(), a.getNrsAgeAdjustment(), a.getNrsTotalScore(), a.getNrsAtRisk(),
                a.getAcuteDiseaseEffect(), a.getMustBmiScore(), a.getMustWeightLossScore(),
                a.getMustAcuteDiseaseScore(), a.getMustTotalScore(), a.getMustRiskCategory(),
                a.getDiseaseCategory(), a.getCaloricTargetMinKcalPerDay(), a.getCaloricTargetMaxKcalPerDay(),
                a.getProteinTargetMinGPerDay(), a.getProteinTargetMaxGPerDay(), a.getFluidRequirementMlPerDay(),
                a.getSodiumRestrictionMeqPerDay(), a.getPotassiumBalanceMeqPerDay(), a.getEnteralParenteralSupport(),
                a.getDieticianAssessment(), a.getWeeklyFollowUpPlan(), a.getCreatedAt()
        );
    }
}
