package com.hms.patient.history.domain;

import com.hms.clinical.complaint.DurationUnit;
import com.hms.common.audit.Auditable;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "patient_history")
public class PatientHistory extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_upid", nullable = false, unique = true, length = 20)
    private String patientUpid;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "patient_history_chronic_diseases", joinColumns = @JoinColumn(name = "patient_history_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "chronic_disease", length = 30)
    private Set<ChronicDiseaseType> chronicDiseases = new HashSet<>();

    @Column(name = "other_chronic_diseases")
    private String otherChronicDiseases;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "patient_history_surgeries", joinColumns = @JoinColumn(name = "patient_history_id"))
    private List<PastSurgery> pastSurgeries = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "patient_history_hospitalizations", joinColumns = @JoinColumn(name = "patient_history_id"))
    private List<PastHospitalization> pastHospitalizations = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "patient_history_medications", joinColumns = @JoinColumn(name = "patient_history_id"))
    private List<CurrentMedication> currentMedications = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "patient_history_allergies", joinColumns = @JoinColumn(name = "patient_history_id"))
    private List<Allergy> allergies = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "patient_history_immunizations", joinColumns = @JoinColumn(name = "patient_history_id"))
    private List<Immunization> immunizations = new ArrayList<>();

    @Column(name = "blood_transfusion_history")
    private Boolean bloodTransfusionHistory;

    @Column(name = "blood_transfusion_details")
    private String bloodTransfusionDetails;

    @Column(name = "current_illness_duration_value")
    private Integer currentIllnessDurationValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_illness_duration_unit", length = 10)
    private DurationUnit currentIllnessDurationUnit;

    @Column(name = "current_illness_onset_date")
    private LocalDate currentIllnessOnsetDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_illness_progression", length = 15)
    private ProgressionType currentIllnessProgression;

    @Column(name = "current_illness_notes", length = 1000)
    private String currentIllnessNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "smoking_status", length = 10)
    private SmokingStatus smokingStatus;

    @Column(name = "smoking_pack_years")
    private Double smokingPackYears;

    @Enumerated(EnumType.STRING)
    @Column(name = "alcohol_frequency", length = 15)
    private AlcoholFrequency alcoholFrequency;

    @Column(name = "alcohol_type")
    private String alcoholType;

    @Column(name = "alcohol_quantity")
    private String alcoholQuantity;

    @Column(name = "alcohol_history_years")
    private Integer alcoholHistoryYears;

    @Enumerated(EnumType.STRING)
    @Column(name = "dietary_habit", length = 20)
    private DietaryHabit dietaryHabit;

    @Enumerated(EnumType.STRING)
    @Column(name = "physical_activity", length = 15)
    private PhysicalActivityLevel physicalActivity;

    @Column(name = "sleep_hours_per_night")
    private Double sleepHoursPerNight;

    @Enumerated(EnumType.STRING)
    @Column(name = "stress_level", length = 10)
    private StressLevel stressLevel;

    @Column(name = "occupation_exposure_history")
    private String occupationExposureHistory;

    @Column(name = "recent_travel_history")
    private String recentTravelHistory;
}
