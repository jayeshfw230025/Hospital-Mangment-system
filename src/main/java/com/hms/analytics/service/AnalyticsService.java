package com.hms.analytics.service;

import com.hms.analytics.domain.Granularity;
import com.hms.analytics.dto.AlosEntry;
import com.hms.analytics.dto.DashboardResponse;
import com.hms.analytics.dto.DiseaseDistributionEntry;
import com.hms.analytics.dto.DiseaseDistributionResponse;
import com.hms.analytics.dto.KpisResponse;
import com.hms.analytics.dto.MortalityEntry;
import com.hms.analytics.dto.NamedCount;
import com.hms.analytics.dto.OpdIpdRatioResponse;
import com.hms.analytics.dto.PatientSatisfactionResponse;
import com.hms.analytics.dto.PatientVolumeBucket;
import com.hms.analytics.dto.PatientVolumeResponse;
import com.hms.analytics.dto.ProcedureStatEntry;
import com.hms.analytics.dto.ReadmissionRateResponse;
import com.hms.analytics.dto.ReferralPatternResponse;
import com.hms.clinical.complaint.SeverityLevel;
import com.hms.clinical.examination.ClinicalExaminationRepository;
import com.hms.clinical.examination.ExaminationContext;
import com.hms.diagnosis.domain.Diagnosis;
import com.hms.diagnosis.domain.Icd10Code;
import com.hms.diagnosis.repository.DiagnosisRepository;
import com.hms.diagnosis.repository.Icd10CodeRepository;
import com.hms.discharge.domain.DischargeSummary;
import com.hms.discharge.domain.DischargeType;
import com.hms.discharge.repository.DischargeSummaryRepository;
import com.hms.ipd.admission.domain.IpdAdmission;
import com.hms.ipd.admission.repository.IpdAdmissionRepository;
import com.hms.ipd.procedure.domain.Procedure;
import com.hms.ipd.procedure.repository.ProcedureComplicationRepository;
import com.hms.ipd.procedure.repository.ProcedureRepository;
import com.hms.ipd.repository.IpdComplaintRepository;
import com.hms.opd.repository.OpdComplaintRepository;
import com.hms.patient.domain.Patient;
import com.hms.patient.repository.PatientRepository;
import com.hms.vitals.repository.OpdVitalsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final PatientRepository patientRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final Icd10CodeRepository icd10CodeRepository;
    private final DischargeSummaryRepository dischargeSummaryRepository;
    private final IpdAdmissionRepository ipdAdmissionRepository;
    private final ProcedureRepository procedureRepository;
    private final ProcedureComplicationRepository procedureComplicationRepository;
    private final OpdComplaintRepository opdComplaintRepository;
    private final IpdComplaintRepository ipdComplaintRepository;
    private final OpdVitalsRepository opdVitalsRepository;
    private final ClinicalExaminationRepository clinicalExaminationRepository;

    public AnalyticsService(PatientRepository patientRepository,
                             DiagnosisRepository diagnosisRepository,
                             Icd10CodeRepository icd10CodeRepository,
                             DischargeSummaryRepository dischargeSummaryRepository,
                             IpdAdmissionRepository ipdAdmissionRepository,
                             ProcedureRepository procedureRepository,
                             ProcedureComplicationRepository procedureComplicationRepository,
                             OpdComplaintRepository opdComplaintRepository,
                             IpdComplaintRepository ipdComplaintRepository,
                             OpdVitalsRepository opdVitalsRepository,
                             ClinicalExaminationRepository clinicalExaminationRepository) {
        this.patientRepository = patientRepository;
        this.diagnosisRepository = diagnosisRepository;
        this.icd10CodeRepository = icd10CodeRepository;
        this.dischargeSummaryRepository = dischargeSummaryRepository;
        this.ipdAdmissionRepository = ipdAdmissionRepository;
        this.procedureRepository = procedureRepository;
        this.procedureComplicationRepository = procedureComplicationRepository;
        this.opdComplaintRepository = opdComplaintRepository;
        this.ipdComplaintRepository = ipdComplaintRepository;
        this.opdVitalsRepository = opdVitalsRepository;
        this.clinicalExaminationRepository = clinicalExaminationRepository;
    }

    @Transactional(readOnly = true)
    public PatientVolumeResponse getPatientVolume(LocalDate startDate, LocalDate endDate, Granularity granularity) {
        List<Patient> patients = filterByDate(patientRepository.findAll(), Patient::getCreatedAt, startDate, endDate);
        List<IpdAdmission> admissions = filterByDate(ipdAdmissionRepository.findAll(), IpdAdmission::getAdmissionDateTime, startDate, endDate);

        Map<String, Long> registrationsByPeriod = patients.stream()
                .collect(Collectors.groupingBy(p -> periodLabel(p.getCreatedAt(), granularity), Collectors.counting()));
        Map<String, Long> admissionsByPeriod = admissions.stream()
                .collect(Collectors.groupingBy(a -> periodLabel(a.getAdmissionDateTime(), granularity), Collectors.counting()));

        Set<String> allPeriods = new HashSet<>();
        allPeriods.addAll(registrationsByPeriod.keySet());
        allPeriods.addAll(admissionsByPeriod.keySet());

        List<PatientVolumeBucket> series = allPeriods.stream()
                .sorted()
                .map(period -> new PatientVolumeBucket(period,
                        registrationsByPeriod.getOrDefault(period, 0L),
                        admissionsByPeriod.getOrDefault(period, 0L)))
                .toList();

        return new PatientVolumeResponse(granularity, series);
    }

    @Transactional(readOnly = true)
    public List<DiseaseDistributionEntry> getTopGiDiseases(int limit) {
        return groupDiagnosesByIcd10(diagnosisRepository.findAll()).stream()
                .sorted(Comparator.comparingLong(DiseaseDistributionEntry::count).reversed())
                .limit(limit)
                .toList();
    }

    @Transactional(readOnly = true)
    public OpdIpdRatioResponse getOpdIpdRatio() {
        Set<Long> opdVisitIds = new HashSet<>();
        opdComplaintRepository.findAll().forEach(c -> opdVisitIds.add(c.getVisitId()));
        opdVitalsRepository.findAll().forEach(v -> opdVisitIds.add(v.getVisitId()));
        clinicalExaminationRepository.findAll().stream()
                .filter(e -> e.getExaminationContext() == ExaminationContext.OPD && e.getVisitId() != null)
                .forEach(e -> opdVisitIds.add(e.getVisitId()));

        long opdEncounters = opdVisitIds.size();
        long ipdAdmissions = ipdAdmissionRepository.count();
        double ratio = ipdAdmissions == 0 ? opdEncounters : round((double) opdEncounters / ipdAdmissions);

        return new OpdIpdRatioResponse(opdEncounters, ipdAdmissions, ratio);
    }

    @Transactional(readOnly = true)
    public List<AlosEntry> getAlosByDiagnosis() {
        Map<String, List<DischargeSummary>> byDiagnosis = dischargeSummaryRepository.findAll().stream()
                .filter(d -> d.getLengthOfStayDays() != null)
                .collect(Collectors.groupingBy(DischargeSummary::getPrimaryDiagnosisIcd10));

        List<AlosEntry> result = new ArrayList<>();
        byDiagnosis.forEach((code, discharges) -> {
            double avg = discharges.stream().mapToInt(DischargeSummary::getLengthOfStayDays).average().orElse(0);
            result.add(new AlosEntry(code, descriptionFor(code), round(avg), discharges.size()));
        });
        return result.stream().sorted(Comparator.comparing(AlosEntry::icd10Code)).toList();
    }

    @Transactional(readOnly = true)
    public ReadmissionRateResponse getReadmissionRate() {
        List<DischargeSummary> discharges = dischargeSummaryRepository.findAll();
        List<IpdAdmission> allAdmissions = ipdAdmissionRepository.findAll();
        Map<Long, IpdAdmission> admissionsById = allAdmissions.stream()
                .collect(Collectors.toMap(IpdAdmission::getId, a -> a));

        if (discharges.isEmpty()) {
            return new ReadmissionRateResponse(0, 0, 0, 0);
        }

        long within7 = 0;
        long within14 = 0;
        long within30 = 0;

        for (DischargeSummary discharge : discharges) {
            IpdAdmission dischargedAdmission = admissionsById.get(discharge.getAdmissionId());
            if (dischargedAdmission == null) {
                continue;
            }
            String patientUpid = dischargedAdmission.getPatientUpid();
            Instant dischargeTime = discharge.getDischargeDateTime();

            boolean readmitted7 = false;
            boolean readmitted14 = false;
            boolean readmitted30 = false;

            for (IpdAdmission candidate : allAdmissions) {
                if (!candidate.getPatientUpid().equals(patientUpid) || candidate.getId().equals(discharge.getAdmissionId())) {
                    continue;
                }
                if (candidate.getAdmissionDateTime().isBefore(dischargeTime)) {
                    continue;
                }
                long daysBetween = ChronoUnit.DAYS.between(dischargeTime, candidate.getAdmissionDateTime());
                if (daysBetween <= 7) {
                    readmitted7 = true;
                }
                if (daysBetween <= 14) {
                    readmitted14 = true;
                }
                if (daysBetween <= 30) {
                    readmitted30 = true;
                }
            }

            if (readmitted7) {
                within7++;
            }
            if (readmitted14) {
                within14++;
            }
            if (readmitted30) {
                within30++;
            }
        }

        int total = discharges.size();
        return new ReadmissionRateResponse(
                round(within7 * 100.0 / total), round(within14 * 100.0 / total), round(within30 * 100.0 / total), total);
    }

    @Transactional(readOnly = true)
    public List<ProcedureStatEntry> getProcedureStats() {
        List<Procedure> procedures = procedureRepository.findAll();
        Set<Long> procedureIdsWithComplication = procedureComplicationRepository.findAll().stream()
                .map(c -> c.getProcedureId())
                .collect(Collectors.toSet());

        Map<String, List<Procedure>> byType = procedures.stream()
                .collect(Collectors.groupingBy(p -> p.getProcedureType().name()));

        List<ProcedureStatEntry> result = new ArrayList<>();
        byType.forEach((typeName, list) -> {
            long total = list.size();
            long withComplications = list.stream().filter(p -> procedureIdsWithComplication.contains(p.getId())).count();
            double complicationRate = total == 0 ? 0 : round(withComplications * 100.0 / total);
            double successRate = round(100.0 - complicationRate);
            result.add(new ProcedureStatEntry(typeName, list.get(0).getProcedureType().getLabel(),
                    total, withComplications, complicationRate, successRate));
        });
        return result.stream().sorted(Comparator.comparing(ProcedureStatEntry::procedureType)).toList();
    }

    @Transactional(readOnly = true)
    public List<MortalityEntry> getMortalityByDiagnosis() {
        Map<String, List<DischargeSummary>> byDiagnosis = dischargeSummaryRepository.findAll().stream()
                .collect(Collectors.groupingBy(DischargeSummary::getPrimaryDiagnosisIcd10));

        List<MortalityEntry> result = new ArrayList<>();
        byDiagnosis.forEach((code, discharges) -> {
            long total = discharges.size();
            long expired = discharges.stream().filter(d -> d.getDischargeType() == DischargeType.EXPIRED).count();
            double rate = total == 0 ? 0 : round(expired * 100.0 / total);
            result.add(new MortalityEntry(code, descriptionFor(code), total, expired, rate));
        });
        return result.stream().sorted(Comparator.comparing(MortalityEntry::icd10Code)).toList();
    }

    public PatientSatisfactionResponse getPatientSatisfaction() {
        return PatientSatisfactionResponse.notAvailable();
    }

    @Transactional(readOnly = true)
    public ReferralPatternResponse getReferralPattern() {
        Map<String, Long> doctorCounts = new HashMap<>();
        Map<String, Long> hospitalCounts = new HashMap<>();

        patientRepository.findAll().forEach(p -> {
            if (p.getReferralDetails() != null) {
                addIfPresent(doctorCounts, p.getReferralDetails().getReferringDoctorName());
                addIfPresent(hospitalCounts, p.getReferralDetails().getReferringHospitalName());
            }
        });

        List<IpdAdmission> admissions = ipdAdmissionRepository.findAll();
        admissions.forEach(a -> {
            addIfPresent(doctorCounts, a.getReferralDoctorName());
            addIfPresent(hospitalCounts, a.getReferringHospitalName());
        });

        Map<String, Long> sourceCounts = admissions.stream()
                .collect(Collectors.groupingBy(a -> a.getAdmissionSource().name(), Collectors.counting()));

        return new ReferralPatternResponse(
                toSortedNamedCounts(doctorCounts), toSortedNamedCounts(hospitalCounts), toSortedNamedCounts(sourceCounts));
    }

    @Transactional(readOnly = true)
    public KpisResponse getKpis(LocalDate startDate, LocalDate endDate, Granularity granularity) {
        return new KpisResponse(
                getPatientVolume(startDate, endDate, granularity),
                getTopGiDiseases(10),
                getOpdIpdRatio(),
                getAlosByDiagnosis(),
                getReadmissionRate(),
                getProcedureStats(),
                getMortalityByDiagnosis(),
                getPatientSatisfaction(),
                getReferralPattern()
        );
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(LocalDate startDate, LocalDate endDate, Granularity granularity) {
        return new DashboardResponse(getKpis(startDate, endDate, granularity), getDiseaseDistribution());
    }

    @Transactional(readOnly = true)
    public DiseaseDistributionResponse getDiseaseDistribution() {
        List<Diagnosis> diagnoses = diagnosisRepository.findAll();
        Map<String, Patient> patientsByUpid = patientRepository.findAll().stream()
                .collect(Collectors.toMap(Patient::getUpid, p -> p));

        Map<String, Long> ageGroupCounts = new HashMap<>();
        Map<String, Long> genderCounts = new HashMap<>();
        Map<String, Long> locationCounts = new HashMap<>();
        Map<String, Long> timeCounts = new HashMap<>();

        for (Diagnosis diagnosis : diagnoses) {
            Patient patient = patientsByUpid.get(diagnosis.getPatientUpid());
            if (patient != null) {
                addIfPresent(ageGroupCounts, ageGroupOf(patient.getDateOfBirth()));
                addIfPresent(genderCounts, patient.getGender() == null ? null : patient.getGender().name());
                addIfPresent(locationCounts, locationLabelOf(patient));
            }
            addIfPresent(timeCounts, monthLabel(diagnosis.getCreatedAt()));
        }

        Map<String, Long> severityCounts = new HashMap<>();
        opdComplaintRepository.findAll().forEach(c -> addIfPresent(severityCounts, labelOf(c.getSeverity())));
        ipdComplaintRepository.findAll().forEach(c -> addIfPresent(severityCounts, labelOf(c.getSeverity())));

        Map<String, Long> outcomeCounts = dischargeSummaryRepository.findAll().stream()
                .collect(Collectors.groupingBy(d -> d.getDischargeType().name(), Collectors.counting()));

        return new DiseaseDistributionResponse(
                toSortedNamedCounts(ageGroupCounts),
                toSortedNamedCounts(genderCounts),
                toSortedNamedCounts(locationCounts),
                toSortedNamedCounts(timeCounts),
                toSortedNamedCounts(severityCounts),
                groupDiagnosesByIcd10(diagnoses),
                toSortedNamedCounts(outcomeCounts)
        );
    }

    private List<DiseaseDistributionEntry> groupDiagnosesByIcd10(List<Diagnosis> diagnoses) {
        Map<String, Long> counts = diagnoses.stream()
                .collect(Collectors.groupingBy(Diagnosis::getIcd10Code, Collectors.counting()));

        return counts.entrySet().stream()
                .map(e -> new DiseaseDistributionEntry(e.getKey(), descriptionFor(e.getKey()), e.getValue()))
                .sorted(Comparator.comparing(DiseaseDistributionEntry::icd10Code))
                .toList();
    }

    private String descriptionFor(String icd10Code) {
        return icd10CodeRepository.findById(icd10Code).map(Icd10Code::getDescription).orElse(null);
    }

    private String labelOf(SeverityLevel severity) {
        return severity == null ? null : severity.name();
    }

    private String ageGroupOf(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return null;
        }
        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        if (age <= 10) {
            return "0-10";
        }
        if (age <= 20) {
            return "11-20";
        }
        if (age <= 40) {
            return "21-40";
        }
        if (age <= 60) {
            return "41-60";
        }
        return "60+";
    }

    private String locationLabelOf(Patient patient) {
        if (patient.getAddress() == null) {
            return null;
        }
        String city = patient.getAddress().getCity();
        String state = patient.getAddress().getState();
        if (city == null && state == null) {
            return null;
        }
        if (city == null) {
            return state;
        }
        if (state == null) {
            return city;
        }
        return city + ", " + state;
    }

    private String monthLabel(Instant instant) {
        return DateTimeFormatter.ofPattern("yyyy-MM").withZone(ZoneOffset.UTC).format(instant);
    }

    private String periodLabel(Instant instant, Granularity granularity) {
        LocalDate date = instant.atZone(ZoneOffset.UTC).toLocalDate();
        return switch (granularity) {
            case DAILY -> date.format(DateTimeFormatter.ISO_LOCAL_DATE);
            case WEEKLY -> date.getYear() + "-W" + String.format("%02d", date.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear()));
            case MONTHLY -> date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            case QUARTERLY -> date.getYear() + "-Q" + ((date.getMonthValue() - 1) / 3 + 1);
        };
    }

    private <T> List<T> filterByDate(List<T> items, Function<T, Instant> dateExtractor,
                                      LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return items;
        }
        Instant start = startDate == null ? Instant.EPOCH : startDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = endDate == null ? Instant.now() : endDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        return items.stream()
                .filter(item -> {
                    Instant value = dateExtractor.apply(item);
                    return !value.isBefore(start) && value.isBefore(end);
                })
                .toList();
    }

    private void addIfPresent(Map<String, Long> counts, String key) {
        if (key != null && !key.isBlank()) {
            counts.merge(key, 1L, Long::sum);
        }
    }

    private List<NamedCount> toSortedNamedCounts(Map<String, Long> counts) {
        return counts.entrySet().stream()
                .map(e -> new NamedCount(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingLong(NamedCount::count).reversed())
                .toList();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
