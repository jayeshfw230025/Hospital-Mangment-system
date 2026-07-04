package com.hms.patient.history.mapper;

import com.hms.patient.history.domain.Allergy;
import com.hms.patient.history.domain.CurrentMedication;
import com.hms.patient.history.domain.Immunization;
import com.hms.patient.history.domain.PastHospitalization;
import com.hms.patient.history.domain.PastSurgery;
import com.hms.patient.history.domain.PatientHistory;
import com.hms.patient.history.dto.AllergyDto;
import com.hms.patient.history.dto.CurrentMedicationDto;
import com.hms.patient.history.dto.ImmunizationDto;
import com.hms.patient.history.dto.LifestyleResponse;
import com.hms.patient.history.dto.PastHospitalizationDto;
import com.hms.patient.history.dto.PastSurgeryDto;
import com.hms.patient.history.dto.PatientHistoryRequest;
import com.hms.patient.history.dto.PatientHistoryResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class PatientHistoryMapper {

    public void applyRequest(PatientHistory history, PatientHistoryRequest request) {
        history.setPatientUpid(request.patientId());

        history.setChronicDiseases(request.chronicDiseases() == null ? new HashSet<>() : new HashSet<>(request.chronicDiseases()));
        history.setOtherChronicDiseases(request.otherChronicDiseases());

        history.setPastSurgeries(nullToEmpty(request.pastSurgeries()).stream()
                .map(s -> new PastSurgery(s.surgeryName(), s.surgeryDate(), s.notes()))
                .collect(Collectors.toCollection(ArrayList::new)));

        history.setPastHospitalizations(nullToEmpty(request.pastHospitalizations()).stream()
                .map(h -> new PastHospitalization(h.reason(), h.admissionDate(), h.dischargeDate(), h.hospitalName()))
                .collect(Collectors.toCollection(ArrayList::new)));

        history.setCurrentMedications(nullToEmpty(request.currentMedications()).stream()
                .map(m -> new CurrentMedication(m.drugName(), m.dosage(), m.frequency()))
                .collect(Collectors.toCollection(ArrayList::new)));

        history.setAllergies(nullToEmpty(request.allergies()).stream()
                .map(a -> new Allergy(a.allergen(), a.reactionType(), a.severity(), a.hardStop()))
                .collect(Collectors.toCollection(ArrayList::new)));

        history.setImmunizations(nullToEmpty(request.immunizations()).stream()
                .map(i -> new Immunization(i.vaccineName(), i.dateAdministered()))
                .collect(Collectors.toCollection(ArrayList::new)));

        history.setBloodTransfusionHistory(request.bloodTransfusionHistory());
        history.setBloodTransfusionDetails(request.bloodTransfusionDetails());

        history.setCurrentIllnessDurationValue(request.currentIllnessDurationValue());
        history.setCurrentIllnessDurationUnit(request.currentIllnessDurationUnit());
        history.setCurrentIllnessOnsetDate(request.currentIllnessOnsetDate());
        history.setCurrentIllnessProgression(request.currentIllnessProgression());
        history.setCurrentIllnessNotes(request.currentIllnessNotes());

        history.setSmokingStatus(request.smokingStatus());
        history.setSmokingPackYears(request.smokingPackYears());
        history.setAlcoholFrequency(request.alcoholFrequency());
        history.setAlcoholType(request.alcoholType());
        history.setAlcoholQuantity(request.alcoholQuantity());
        history.setAlcoholHistoryYears(request.alcoholHistoryYears());
        history.setDietaryHabit(request.dietaryHabit());
        history.setPhysicalActivity(request.physicalActivity());
        history.setSleepHoursPerNight(request.sleepHoursPerNight());
        history.setStressLevel(request.stressLevel());
        history.setOccupationExposureHistory(request.occupationExposureHistory());
        history.setRecentTravelHistory(request.recentTravelHistory());
    }

    public PatientHistoryResponse toResponse(PatientHistory history) {
        return new PatientHistoryResponse(
                history.getId(),
                history.getPatientUpid(),
                history.getChronicDiseases(),
                history.getOtherChronicDiseases(),
                history.getPastSurgeries().stream()
                        .map(s -> new PastSurgeryDto(s.getSurgeryName(), s.getSurgeryDate(), s.getNotes())).toList(),
                history.getPastHospitalizations().stream()
                        .map(h -> new PastHospitalizationDto(h.getReason(), h.getAdmissionDate(), h.getDischargeDate(), h.getHospitalName())).toList(),
                history.getCurrentMedications().stream()
                        .map(m -> new CurrentMedicationDto(m.getDrugName(), m.getDosage(), m.getFrequency())).toList(),
                history.getAllergies().stream()
                        .map(a -> new AllergyDto(a.getAllergen(), a.getReactionType(), a.getSeverity(), a.isHardStop())).toList(),
                history.getImmunizations().stream()
                        .map(i -> new ImmunizationDto(i.getVaccineName(), i.getDateAdministered())).toList(),
                history.getBloodTransfusionHistory(),
                history.getBloodTransfusionDetails(),
                history.getCurrentIllnessDurationValue(),
                history.getCurrentIllnessDurationUnit(),
                history.getCurrentIllnessOnsetDate(),
                history.getCurrentIllnessProgression(),
                history.getCurrentIllnessNotes(),
                history.getSmokingStatus(),
                history.getSmokingPackYears(),
                history.getAlcoholFrequency(),
                history.getAlcoholType(),
                history.getAlcoholQuantity(),
                history.getAlcoholHistoryYears(),
                history.getDietaryHabit(),
                history.getPhysicalActivity(),
                history.getSleepHoursPerNight(),
                history.getStressLevel(),
                history.getOccupationExposureHistory(),
                history.getRecentTravelHistory(),
                history.getCreatedAt()
        );
    }

    public LifestyleResponse toLifestyleResponse(PatientHistory history) {
        return new LifestyleResponse(
                history.getPatientUpid(),
                history.getSmokingStatus(),
                history.getSmokingPackYears(),
                history.getAlcoholFrequency(),
                history.getAlcoholType(),
                history.getAlcoholQuantity(),
                history.getAlcoholHistoryYears(),
                history.getDietaryHabit(),
                history.getPhysicalActivity(),
                history.getSleepHoursPerNight(),
                history.getStressLevel(),
                history.getOccupationExposureHistory(),
                history.getRecentTravelHistory()
        );
    }

    private <T> List<T> nullToEmpty(List<T> list) {
        return Objects.requireNonNullElse(list, List.of());
    }
}
