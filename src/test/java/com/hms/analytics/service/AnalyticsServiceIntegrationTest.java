package com.hms.analytics.service;

import com.hms.analytics.domain.Granularity;
import com.hms.analytics.dto.DiseaseDistributionResponse;
import com.hms.analytics.dto.KpisResponse;
import com.hms.analytics.dto.NamedCount;
import com.hms.analytics.dto.OpdIpdRatioResponse;
import com.hms.analytics.dto.ProcedureStatEntry;
import com.hms.analytics.dto.ReadmissionRateResponse;
import com.hms.clinical.complaint.FrequencyLevel;
import com.hms.clinical.complaint.SeverityLevel;
import com.hms.diagnosis.domain.DiagnosisType;
import com.hms.diagnosis.dto.DiagnosisRequest;
import com.hms.diagnosis.service.DiagnosisService;
import com.hms.discharge.domain.DischargeCondition;
import com.hms.discharge.domain.DischargeType;
import com.hms.discharge.dto.DischargeSummaryRequest;
import com.hms.discharge.service.DischargeSummaryService;
import com.hms.ipd.admission.domain.AdmissionSource;
import com.hms.ipd.admission.domain.AdmissionType;
import com.hms.ipd.admission.dto.IpdAdmissionRequest;
import com.hms.ipd.admission.service.IpdAdmissionService;
import com.hms.ipd.procedure.domain.ProcedureType;
import com.hms.ipd.procedure.dto.ProcedureComplicationRequest;
import com.hms.ipd.procedure.dto.ProcedureRequest;
import com.hms.ipd.procedure.dto.ProcedureResponse;
import com.hms.ipd.procedure.service.ProcedureComplicationService;
import com.hms.ipd.procedure.service.ProcedureService;
import com.hms.opd.dto.OpdComplaintRequest;
import com.hms.opd.service.OpdComplaintService;
import com.hms.clinical.complaint.ComplaintType;
import com.hms.patient.domain.Gender;
import com.hms.patient.dto.PatientRegistrationRequest;
import com.hms.patient.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AnalyticsServiceIntegrationTest {

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private AnalyticsExportService analyticsExportService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private IpdAdmissionService ipdAdmissionService;

    @Autowired
    private DiagnosisService diagnosisService;

    @Autowired
    private DischargeSummaryService dischargeSummaryService;

    @Autowired
    private ProcedureService procedureService;

    @Autowired
    private ProcedureComplicationService procedureComplicationService;

    @Autowired
    private OpdComplaintService opdComplaintService;

    private String registerPatient(String contactNumber) {
        PatientRegistrationRequest request = new PatientRegistrationRequest(
                null, "Analytics Test Patient", LocalDate.of(1985, 1, 1), Gender.MALE, null, null,
                "Indian", null, null, null, contactNumber, null, null, null, null,
                null, null, null, null, null
        );
        return patientService.register(request).upid();
    }

    private Long createAdmission(String patientId, String icd10Code) {
        IpdAdmissionRequest request = new IpdAdmissionRequest(
                patientId, AdmissionType.EMERGENCY, AdmissionSource.ER,
                null, null, null, null, icd10Code, null, "Analytics test admission", "Signed");
        return ipdAdmissionService.create(request, null).id();
    }

    @Test
    void patientVolumeIncreasesByOneAfterNewRegistration() {
        var before = analyticsService.getPatientVolume(null, null, Granularity.MONTHLY);
        long beforeCount = currentMonthCount(before);

        registerPatient("9101900001");

        var after = analyticsService.getPatientVolume(null, null, Granularity.MONTHLY);
        long afterCount = currentMonthCount(after);

        assertThat(afterCount).isEqualTo(beforeCount + 1);
    }

    private long currentMonthCount(com.hms.analytics.dto.PatientVolumeResponse response) {
        String currentMonth = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM").format(LocalDate.now());
        return response.series().stream()
                .filter(b -> b.periodLabel().equals(currentMonth))
                .mapToLong(b -> b.newRegistrations())
                .sum();
    }

    @Test
    void diseaseDistributionCountsIncreaseAfterNewDiagnosis() {
        long before = countForIcd10("K60.0");

        String patientId = registerPatient("9101900002");
        diagnosisService.create(new DiagnosisRequest(patientId, "K60.0", DiagnosisType.PRIMARY, null, null, null));

        long after = countForIcd10("K60.0");

        assertThat(after).isEqualTo(before + 1);
    }

    private long countForIcd10(String code) {
        return analyticsService.getTopGiDiseases(1000).stream()
                .filter(e -> e.icd10Code().equals(code))
                .mapToLong(e -> e.count())
                .sum();
    }

    @Test
    void opdIpdRatioReflectsNewAdmissionAndNewOpdEncounter() {
        OpdIpdRatioResponse before = analyticsService.getOpdIpdRatio();

        String patientId = registerPatient("9101900003");
        createAdmission(patientId, "K25.0");
        opdComplaintService.create(new OpdComplaintRequest(
                88880001L, ComplaintType.BLOATING, SeverityLevel.MILD, 1, null, FrequencyLevel.RARE,
                LocalDate.now(), null, Map.of()));

        OpdIpdRatioResponse after = analyticsService.getOpdIpdRatio();

        assertThat(after.ipdAdmissions()).isEqualTo(before.ipdAdmissions() + 1);
        assertThat(after.opdEncounters()).isEqualTo(before.opdEncounters() + 1);
    }

    @Test
    void alosEntryAppearsForNewDiagnosisAfterDischarge() {
        String patientId = registerPatient("9101900004");
        Long admissionId = createAdmission(patientId, "K61.0");
        dischargeSummaryService.create(new DischargeSummaryRequest(
                admissionId, DischargeType.RECOVERED, "K61.0", null, "Resolved", "Uneventful",
                null, null, DischargeCondition.STABLE, "Dr. Test", "Signed", true, null, null));

        var alos = analyticsService.getAlosByDiagnosis();

        assertThat(alos).anyMatch(e -> e.icd10Code().equals("K61.0"));
    }

    @Test
    void readmissionIsDetectedForSameDayReadmission() {
        ReadmissionRateResponse before = analyticsService.getReadmissionRate();

        String patientId = registerPatient("9101900005");
        Long admission1 = createAdmission(patientId, "K25.0");
        dischargeSummaryService.create(new DischargeSummaryRequest(
                admission1, DischargeType.RECOVERED, "K25.0", null, null, null,
                null, null, DischargeCondition.STABLE, "Dr. Test", "Signed", true, null, null));
        createAdmission(patientId, "K25.0");

        ReadmissionRateResponse after = analyticsService.getReadmissionRate();

        assertThat(after.totalDischargesConsidered()).isEqualTo(before.totalDischargesConsidered() + 1);
        assertThat(after.rate7DayPercent()).isGreaterThan(0.0);
    }

    @Test
    void procedureStatsTrackCountsAndComplicationsForErcp() {
        var before = findEntry(analyticsService.getProcedureStats(), "ERCP");
        long beforeTotal = before == null ? 0 : before.totalCount();
        long beforeComplications = before == null ? 0 : before.complicationCount();

        String patientId = registerPatient("9101900006");
        Long admissionId = createAdmission(patientId, "K25.0");

        ProcedureResponse p1 = procedureService.create(new ProcedureRequest(
                admissionId, ProcedureType.ERCP, LocalDate.now(), "Dr. Test", null,
                Map.of("cannulationSuccess", "true", "findings", "Common bile duct stone")));
        procedureComplicationService.create(new ProcedureComplicationRequest(
                p1.id(), "Post-ERCP pancreatitis", SeverityLevel.MODERATE, LocalDate.now(), "Dr. Test"));
        procedureService.create(new ProcedureRequest(
                admissionId, ProcedureType.ERCP, LocalDate.now(), "Dr. Test", null,
                Map.of("cannulationSuccess", "true", "findings", "Normal")));

        var afterEntry = findEntry(analyticsService.getProcedureStats(), "ERCP");

        assertThat(afterEntry).isNotNull();
        assertThat(afterEntry.totalCount()).isEqualTo(beforeTotal + 2);
        assertThat(afterEntry.complicationCount()).isEqualTo(beforeComplications + 1);
    }

    private ProcedureStatEntry findEntry(java.util.List<ProcedureStatEntry> entries, String type) {
        return entries.stream().filter(e -> e.procedureType().equals(type)).findFirst().orElse(null);
    }

    @Test
    void mortalityRateTracksExpiredDischarges() {
        long beforeExpired = expiredCountFor("K62.4");

        String patientId = registerPatient("9101900007");
        Long admissionId = createAdmission(patientId, "K62.4");
        dischargeSummaryService.create(new DischargeSummaryRequest(
                admissionId, DischargeType.EXPIRED, "K62.4", null, null, null,
                null, null, DischargeCondition.STABLE, "Dr. Test", "Signed", true, null, null));

        long afterExpired = expiredCountFor("K62.4");

        assertThat(afterExpired).isEqualTo(beforeExpired + 1);
    }

    private long expiredCountFor(String code) {
        return analyticsService.getMortalityByDiagnosis().stream()
                .filter(e -> e.icd10Code().equals(code))
                .mapToLong(e -> e.expiredCount())
                .sum();
    }

    @Test
    void referralPatternIncludesNewlyRegisteredReferringDoctor() {
        PatientRegistrationRequest request = new PatientRegistrationRequest(
                null, "Referral Test Patient", LocalDate.of(1980, 1, 1), Gender.FEMALE, null, null,
                "Indian", null, null, null, "9101900008", null, null, null, null,
                null, null, null, null,
                new com.hms.patient.dto.ReferralDetailsDto(
                        "Dr. Unique Analytics Referrer", "Analytics Test Hospital", LocalDate.now(), "Routine", "9998887777")
        );
        patientService.register(request);

        var referralPattern = analyticsService.getReferralPattern();

        assertThat(referralPattern.topReferringDoctors()).extracting(NamedCount::label)
                .contains("Dr. Unique Analytics Referrer");
    }

    @Test
    void patientSatisfactionIsHonestlyReportedAsUnavailable() {
        var satisfaction = analyticsService.getPatientSatisfaction();

        assertThat(satisfaction.available()).isFalse();
        assertThat(satisfaction.message()).isNotBlank();
    }

    @Test
    void diseaseDistributionReturnsAllSevenDimensions() {
        DiseaseDistributionResponse distribution = analyticsService.getDiseaseDistribution();

        assertThat(distribution.byAgeGroup()).isNotNull();
        assertThat(distribution.byGender()).isNotNull();
        assertThat(distribution.byLocation()).isNotNull();
        assertThat(distribution.byTime()).isNotNull();
        assertThat(distribution.byClinicalSeverity()).isNotNull();
        assertThat(distribution.byIcd10Code()).isNotEmpty();
        assertThat(distribution.byTreatmentOutcome()).isNotEmpty();
    }

    @Test
    void exportsCsvAndPdfWithRealContent() {
        KpisResponse kpis = analyticsService.getKpis(null, null, Granularity.MONTHLY);

        byte[] csv = analyticsExportService.generateCsv(kpis);
        byte[] pdf = analyticsExportService.generatePdf(kpis);

        assertThat(new String(csv, java.nio.charset.StandardCharsets.UTF_8)).contains("Patient Volume");
        assertThat(new String(pdf, 0, 5, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }
}
