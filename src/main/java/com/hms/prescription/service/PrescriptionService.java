package com.hms.prescription.service;

import com.hms.common.exception.ResourceNotFoundException;
import com.hms.prescription.domain.Drug;
import com.hms.prescription.domain.Prescription;
import com.hms.prescription.domain.PrescriptionItem;
import com.hms.prescription.dto.DrugInteractionCheckRequest;
import com.hms.prescription.dto.DrugInteractionCheckResponse;
import com.hms.prescription.dto.DrugInteractionWarning;
import com.hms.prescription.dto.NutritionAlert;
import com.hms.prescription.dto.PrescriptionItemRequest;
import com.hms.prescription.dto.PrescriptionItemResponse;
import com.hms.prescription.dto.PrescriptionRequest;
import com.hms.prescription.dto.PrescriptionResponse;
import com.hms.prescription.repository.DrugRepository;
import com.hms.prescription.repository.PrescriptionRepository;
import com.hms.patient.repository.PatientRepository;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final DrugRepository drugRepository;
    private final PatientRepository patientRepository;
    private final PrescriptionSafetyService prescriptionSafetyService;
    private final PrescriptionInstructionBuilder instructionBuilder;

    public PrescriptionService(PrescriptionRepository prescriptionRepository,
                                DrugRepository drugRepository,
                                PatientRepository patientRepository,
                                PrescriptionSafetyService prescriptionSafetyService,
                                PrescriptionInstructionBuilder instructionBuilder) {
        this.prescriptionRepository = prescriptionRepository;
        this.drugRepository = drugRepository;
        this.patientRepository = patientRepository;
        this.prescriptionSafetyService = prescriptionSafetyService;
        this.instructionBuilder = instructionBuilder;
    }

    @Transactional
    public PrescriptionResponse create(PrescriptionRequest request) {
        if (patientRepository.findByUpid(request.patientId()).isEmpty()) {
            throw new ResourceNotFoundException("Patient not found with UPID: " + request.patientId());
        }

        Map<Long, Drug> drugsById = request.items().stream()
                .map(PrescriptionItemRequest::drugId)
                .distinct()
                .collect(java.util.stream.Collectors.toMap(id -> id, this::requireDrug));

        List<Drug> drugs = drugsById.values().stream().toList();
        prescriptionSafetyService.enforceAllergyHardStop(request.patientId(), drugs);
        List<DrugInteractionWarning> interactionWarnings = prescriptionSafetyService.checkInteractions(request.patientId(), drugs);
        List<NutritionAlert> nutritionAlerts = prescriptionSafetyService.getNutritionAlerts(drugs);

        Prescription prescription = new Prescription();
        prescription.setPatientUpid(request.patientId());
        prescription.setVisitId(request.visitId());
        prescription.setAdmissionId(request.admissionId());
        prescription.setPrescribedDate(LocalDate.now());
        prescription.setDoctorName(request.doctorName());
        prescription.setDigitalSignature(request.digitalSignature());
        prescription.setTemplateUsed(request.templateUsed());
        prescription.setSignatureHash(computeSignatureHash(request));

        List<PrescriptionItem> items = request.items().stream()
                .map(itemRequest -> toItem(itemRequest, drugsById.get(itemRequest.drugId()), request.patientId()))
                .toList();
        prescription.setItems(items);

        Prescription saved = prescriptionRepository.save(prescription);

        return toResponse(saved, drugsById, interactionWarnings, nutritionAlerts);
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getByPatientId(String patientId) {
        return prescriptionRepository.findByPatientUpidOrderByCreatedAtDesc(patientId).stream()
                .map(prescription -> toResponse(prescription, loadDrugsForItems(prescription), List.of(), List.of()))
                .toList();
    }

    @Transactional(readOnly = true)
    public DrugInteractionCheckResponse checkInteractions(DrugInteractionCheckRequest request) {
        List<Drug> drugs = request.drugIds().stream().distinct().map(this::requireDrug).toList();

        List<DrugInteractionWarning> warnings = prescriptionSafetyService.checkInteractions(request.patientId(), drugs);
        List<NutritionAlert> nutritionAlerts = prescriptionSafetyService.getNutritionAlerts(drugs);

        return new DrugInteractionCheckResponse(warnings, nutritionAlerts);
    }

    @Transactional(readOnly = true)
    public Prescription getEntityById(Long id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with id: " + id));
        Hibernate.initialize(prescription.getItems());
        return prescription;
    }

    private Drug requireDrug(Long drugId) {
        Drug drug = drugRepository.findById(drugId)
                .orElseThrow(() -> new ResourceNotFoundException("Drug not found with id: " + drugId));
        if (!drug.isActive()) {
            throw new IllegalArgumentException("Drug " + drug.getGenericName() + " is inactive and cannot be prescribed");
        }
        return drug;
    }

    private PrescriptionItem toItem(PrescriptionItemRequest itemRequest, Drug drug, String patientUpid) {
        PrescriptionItem item = new PrescriptionItem();
        item.setDrugId(drug.getId());
        item.setGenericName(drug.getGenericName());
        item.setDosage(itemRequest.dosage());
        item.setFrequency(itemRequest.frequency());
        item.setRoute(itemRequest.route());
        item.setDurationDays(itemRequest.durationDays());
        item.setFoodInstruction(itemRequest.foodInstruction());
        item.setGeneratedInstructions(instructionBuilder.build(itemRequest, drug.getGenericName()));
        item.setRefillsAllowed(itemRequest.refillsAllowed() == null ? 0 : itemRequest.refillsAllowed());
        item.setRefillsUsed(0);
        item.setCalculatedPediatricDoseMg(
                prescriptionSafetyService.calculatePediatricDoseMg(patientUpid, drug, itemRequest.patientWeightKg()));
        return item;
    }

    private Map<Long, Drug> loadDrugsForItems(Prescription prescription) {
        return prescription.getItems().stream()
                .map(PrescriptionItem::getDrugId)
                .distinct()
                .collect(java.util.stream.Collectors.toMap(id -> id, id -> drugRepository.findById(id).orElse(null)));
    }

    private PrescriptionResponse toResponse(Prescription prescription, Map<Long, Drug> drugsById,
                                             List<DrugInteractionWarning> interactionWarnings,
                                             List<NutritionAlert> nutritionAlerts) {
        List<PrescriptionItemResponse> itemResponses = prescription.getItems().stream()
                .map(item -> {
                    Drug drug = drugsById.get(item.getDrugId());
                    return new PrescriptionItemResponse(
                            item.getDrugId(),
                            item.getGenericName(),
                            drug == null ? null : drug.getBrandName(),
                            item.getDosage(),
                            item.getFrequency(),
                            item.getRoute(),
                            item.getDurationDays(),
                            item.getFoodInstruction(),
                            item.getGeneratedInstructions(),
                            item.getRefillsAllowed(),
                            item.getRefillsUsed(),
                            item.getCalculatedPediatricDoseMg()
                    );
                })
                .toList();

        return new PrescriptionResponse(
                prescription.getId(),
                prescription.getPatientUpid(),
                prescription.getVisitId(),
                prescription.getAdmissionId(),
                prescription.getPrescribedDate(),
                prescription.getDoctorName(),
                prescription.getTemplateUsed(),
                itemResponses,
                interactionWarnings,
                nutritionAlerts,
                prescription.getCreatedAt()
        );
    }

    private String computeSignatureHash(PrescriptionRequest request) {
        try {
            String payload = request.patientId() + "|" + request.doctorName() + "|" + request.digitalSignature()
                    + "|" + LocalDate.now();
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(sha256.digest(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to compute signature hash", e);
        }
    }
}
