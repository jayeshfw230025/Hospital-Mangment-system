package com.hms.patient.history.dto;

import com.hms.clinical.complaint.DurationUnit;
import com.hms.patient.history.domain.AlcoholFrequency;
import com.hms.patient.history.domain.ChronicDiseaseType;
import com.hms.patient.history.domain.DietaryHabit;
import com.hms.patient.history.domain.PhysicalActivityLevel;
import com.hms.patient.history.domain.ProgressionType;
import com.hms.patient.history.domain.SmokingStatus;
import com.hms.patient.history.domain.StressLevel;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record PatientHistoryRequest(

        @NotBlank(message = "Patient ID (UPID) is required")
        String patientId,

        Set<ChronicDiseaseType> chronicDiseases,
        String otherChronicDiseases,

        List<PastSurgeryDto> pastSurgeries,
        List<PastHospitalizationDto> pastHospitalizations,
        List<CurrentMedicationDto> currentMedications,
        List<AllergyDto> allergies,
        List<ImmunizationDto> immunizations,

        Boolean bloodTransfusionHistory,
        String bloodTransfusionDetails,

        Integer currentIllnessDurationValue,
        DurationUnit currentIllnessDurationUnit,
        LocalDate currentIllnessOnsetDate,
        ProgressionType currentIllnessProgression,
        String currentIllnessNotes,

        SmokingStatus smokingStatus,
        Double smokingPackYears,
        AlcoholFrequency alcoholFrequency,
        String alcoholType,
        String alcoholQuantity,
        Integer alcoholHistoryYears,
        DietaryHabit dietaryHabit,
        PhysicalActivityLevel physicalActivity,
        Double sleepHoursPerNight,
        StressLevel stressLevel,
        String occupationExposureHistory,
        String recentTravelHistory
) {
}
