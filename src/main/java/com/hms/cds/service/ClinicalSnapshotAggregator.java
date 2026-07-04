package com.hms.cds.service;

import com.hms.cds.domain.AdditionalFindings;
import com.hms.cds.domain.ClinicalSnapshot;
import com.hms.clinical.complaint.ComplaintType;
import com.hms.clinical.examination.AbdominalExamination;
import com.hms.clinical.examination.AscitesAssessment;
import com.hms.clinical.examination.ClinicalExamination;
import com.hms.clinical.examination.ClinicalExaminationRepository;
import com.hms.clinical.examination.GiMassExamination;
import com.hms.clinical.examination.JaundiceAssessment;
import com.hms.investigation.domain.InvestigationReport;
import com.hms.investigation.domain.ResultParameter;
import com.hms.investigation.repository.InvestigationReportRepository;
import com.hms.ipd.domain.IpdComplaint;
import com.hms.ipd.repository.IpdComplaintRepository;
import com.hms.opd.domain.OpdComplaint;
import com.hms.opd.repository.OpdComplaintRepository;
import com.hms.patient.domain.Patient;
import com.hms.patient.history.repository.FamilyHistoryRepository;
import com.hms.patient.repository.PatientRepository;
import com.hms.vitals.domain.CoreVitals;
import com.hms.vitals.domain.IpdVitals;
import com.hms.vitals.domain.OpdVitals;
import com.hms.vitals.repository.IpdVitalsRepository;
import com.hms.vitals.repository.OpdVitalsRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Aggregates signals scattered across Modules 1-8 (complaints, vitals, clinical
 * exam, family history, investigation results) into one ClinicalSnapshot for the
 * alert rules to evaluate. Deliberately depends on many other bounded contexts -
 * that breadth is inherent to what a clinical decision support engine does.
 */
@Component
public class ClinicalSnapshotAggregator {

    private final PatientRepository patientRepository;
    private final OpdComplaintRepository opdComplaintRepository;
    private final IpdComplaintRepository ipdComplaintRepository;
    private final OpdVitalsRepository opdVitalsRepository;
    private final IpdVitalsRepository ipdVitalsRepository;
    private final ClinicalExaminationRepository clinicalExaminationRepository;
    private final FamilyHistoryRepository familyHistoryRepository;
    private final InvestigationReportRepository investigationReportRepository;

    public ClinicalSnapshotAggregator(PatientRepository patientRepository,
                                    OpdComplaintRepository opdComplaintRepository,
                                    IpdComplaintRepository ipdComplaintRepository,
                                    OpdVitalsRepository opdVitalsRepository,
                                    IpdVitalsRepository ipdVitalsRepository,
                                    ClinicalExaminationRepository clinicalExaminationRepository,
                                    FamilyHistoryRepository familyHistoryRepository,
                                    InvestigationReportRepository investigationReportRepository) {
        this.patientRepository = patientRepository;
        this.opdComplaintRepository = opdComplaintRepository;
        this.ipdComplaintRepository = ipdComplaintRepository;
        this.opdVitalsRepository = opdVitalsRepository;
        this.ipdVitalsRepository = ipdVitalsRepository;
        this.clinicalExaminationRepository = clinicalExaminationRepository;
        this.familyHistoryRepository = familyHistoryRepository;
        this.investigationReportRepository = investigationReportRepository;
    }

    public ClinicalSnapshot build(String patientUpid, Long visitId, Long admissionId, AdditionalFindings additionalFindings) {
        ClinicalSnapshot.ClinicalSnapshotBuilder builder = ClinicalSnapshot.builder()
                .ageYears(resolveAge(patientUpid))
                .complaints(resolveComplaints(visitId, admissionId))
                .familyHistoryGiMalignancy(resolveFamilyHistory(patientUpid))
                .additionalFindings(additionalFindings == null ? AdditionalFindings.none() : additionalFindings);

        applyVitals(builder, visitId, admissionId);
        applyExamFindings(builder, visitId, admissionId);
        applyLabFindings(builder, patientUpid);

        return builder.build();
    }

    private Integer resolveAge(String patientUpid) {
        return patientRepository.findByUpid(patientUpid)
                .map(Patient::getDateOfBirth)
                .map(dob -> Period.between(dob, LocalDate.now()).getYears())
                .orElse(null);
    }

    private Set<ComplaintType> resolveComplaints(Long visitId, Long admissionId) {
        Set<ComplaintType> complaints = new java.util.HashSet<>();
        if (visitId != null) {
            complaints.addAll(opdComplaintRepository.findByVisitIdOrderByCreatedAtAsc(visitId).stream()
                    .map(OpdComplaint::getComplaintType).collect(Collectors.toSet()));
        }
        if (admissionId != null) {
            complaints.addAll(ipdComplaintRepository.findByAdmissionIdOrderByCreatedAtAsc(admissionId).stream()
                    .map(IpdComplaint::getComplaintType).collect(Collectors.toSet()));
        }
        return complaints;
    }

    private boolean resolveFamilyHistory(String patientUpid) {
        return familyHistoryRepository.findByPatientUpid(patientUpid)
                .map(h -> h.isGiMalignancy())
                .orElse(false);
    }

    private void applyVitals(ClinicalSnapshot.ClinicalSnapshotBuilder builder, Long visitId, Long admissionId) {
        if (admissionId != null) {
            List<IpdVitals> vitals = ipdVitalsRepository.findByAdmissionIdOrderByCreatedAtAsc(admissionId);
            if (!vitals.isEmpty()) {
                IpdVitals latest = vitals.get(vitals.size() - 1);
                CoreVitals c = latest.getCoreVitals();
                builder.systolicBp(c.getSystolicBp()).heartRate(c.getHeartRate())
                        .temperatureCelsius(c.getTemperatureCelsius()).spo2(c.getSpo2())
                        .gcsScore(latest.getGcsScore());
            }
            return;
        }
        if (visitId != null) {
            List<OpdVitals> vitals = opdVitalsRepository.findByVisitIdOrderByCreatedAtAsc(visitId);
            if (!vitals.isEmpty()) {
                CoreVitals c = vitals.get(vitals.size() - 1).getCoreVitals();
                builder.systolicBp(c.getSystolicBp()).heartRate(c.getHeartRate())
                        .temperatureCelsius(c.getTemperatureCelsius()).spo2(c.getSpo2());
            }
        }
    }

    private void applyExamFindings(ClinicalSnapshot.ClinicalSnapshotBuilder builder, Long visitId, Long admissionId) {
        ClinicalExamination latestExam = null;
        if (admissionId != null) {
            List<ClinicalExamination> exams = clinicalExaminationRepository.findByAdmissionIdOrderByCreatedAtAsc(admissionId);
            latestExam = exams.isEmpty() ? null : exams.get(exams.size() - 1);
        } else if (visitId != null) {
            List<ClinicalExamination> exams = clinicalExaminationRepository.findByVisitIdOrderByCreatedAtAsc(visitId);
            latestExam = exams.isEmpty() ? null : exams.get(exams.size() - 1);
        }
        if (latestExam == null) {
            return;
        }

        AbdominalExamination abdominal = latestExam.getAbdominalExamination();
        if (abdominal != null && Boolean.TRUE.equals(abdominal.getRigidity())) {
            builder.rigidityPresent(true);
        }

        GiMassExamination giMass = latestExam.getGiMassExamination();
        if (giMass != null && Boolean.TRUE.equals(giMass.getMassPresent())) {
            builder.giMassPresent(true);
        }

        AscitesAssessment ascites = latestExam.getAscitesAssessment();
        if (ascites != null && Boolean.TRUE.equals(ascites.getShiftingDullnessPresent())) {
            builder.ascitesPresent(true);
        }

        JaundiceAssessment jaundice = latestExam.getJaundiceAssessment();
        if (jaundice != null && (Boolean.TRUE.equals(jaundice.getIcterusSclera()) || Boolean.TRUE.equals(jaundice.getIcterusSkin()))) {
            builder.jaundiceExamPresent(true);
        }
    }

    private void applyLabFindings(ClinicalSnapshot.ClinicalSnapshotBuilder builder, String patientUpid) {
        List<InvestigationReport> reports = investigationReportRepository
                .findByPatientUpidOrderByReportDateDescIdDesc(patientUpid);

        double[] hemoglobin = findLatestAndPrevious(reports, "Hemoglobin");
        double[] creatinine = findLatestAndPrevious(reports, "Creatinine");
        double[] platelets = findLatestAndPrevious(reports, "Platelets");

        if (!Double.isNaN(hemoglobin[0])) {
            builder.hemoglobinLatest(hemoglobin[0]);
        }
        if (!Double.isNaN(hemoglobin[1])) {
            builder.hemoglobinPrevious(hemoglobin[1]);
        }
        if (!Double.isNaN(creatinine[0])) {
            builder.creatinineLatest(creatinine[0]);
        }
        if (!Double.isNaN(creatinine[1])) {
            builder.creatininePrevious(creatinine[1]);
        }
        if (!Double.isNaN(platelets[0])) {
            builder.plateletsLatest(platelets[0]);
        }

        Double creatinineRefHigh = findLatestReferenceHigh(reports, "Creatinine");
        Double plateletsRefLow = findLatestReferenceLow(reports, "Platelets");
        builder.creatinineReferenceHigh(creatinineRefHigh);
        builder.plateletsReferenceLow(plateletsRefLow);

        builder.ptOrInrAbnormal(hasAbnormalParameter(reports, Set.of("PT", "INR")));
        builder.hPyloriPositive(hasPositiveResult(reports, "H_PYLORI"));
        builder.lftElevatedOver3x(hasLftElevatedOver3x(reports));
    }

    private double[] findLatestAndPrevious(List<InvestigationReport> reportsDesc, String parameterName) {
        Double latest = null;
        Double previous = null;
        for (InvestigationReport report : reportsDesc) {
            for (ResultParameter param : report.getResultParameters()) {
                if (!parameterName.equalsIgnoreCase(param.getParameterName())) {
                    continue;
                }
                Double value = parseDoubleOrNull(param.getValue());
                if (value == null) {
                    continue;
                }
                if (latest == null) {
                    latest = value;
                } else if (previous == null) {
                    previous = value;
                }
            }
            if (latest != null && previous != null) {
                break;
            }
        }
        return new double[]{latest == null ? Double.NaN : latest, previous == null ? Double.NaN : previous};
    }

    private Double findLatestReferenceHigh(List<InvestigationReport> reportsDesc, String parameterName) {
        for (InvestigationReport report : reportsDesc) {
            for (ResultParameter param : report.getResultParameters()) {
                if (parameterName.equalsIgnoreCase(param.getParameterName()) && param.getReferenceRangeHigh() != null) {
                    return param.getReferenceRangeHigh();
                }
            }
        }
        return null;
    }

    private Double findLatestReferenceLow(List<InvestigationReport> reportsDesc, String parameterName) {
        for (InvestigationReport report : reportsDesc) {
            for (ResultParameter param : report.getResultParameters()) {
                if (parameterName.equalsIgnoreCase(param.getParameterName()) && param.getReferenceRangeLow() != null) {
                    return param.getReferenceRangeLow();
                }
            }
        }
        return null;
    }

    private boolean hasAbnormalParameter(List<InvestigationReport> reports, Set<String> parameterNames) {
        return reports.stream().flatMap(r -> r.getResultParameters().stream())
                .anyMatch(p -> parameterNames.stream().anyMatch(name -> name.equalsIgnoreCase(p.getParameterName())) && p.isAbnormal());
    }

    private boolean hasPositiveResult(List<InvestigationReport> reports, String investigationTypeCode) {
        return reports.stream()
                .filter(r -> investigationTypeCode.equals(r.getInvestigationTypeCode()))
                .flatMap(r -> r.getResultParameters().stream())
                .anyMatch(p -> p.isAbnormal() || (p.getValue() != null && p.getValue().toLowerCase().contains("positive")));
    }

    private boolean hasLftElevatedOver3x(List<InvestigationReport> reports) {
        return reports.stream()
                .filter(r -> "LFT".equals(r.getInvestigationTypeCode()))
                .flatMap(r -> r.getResultParameters().stream())
                .anyMatch(p -> {
                    Double value = parseDoubleOrNull(p.getValue());
                    return value != null && p.getReferenceRangeHigh() != null && value > 3 * p.getReferenceRangeHigh();
                });
    }

    private Double parseDoubleOrNull(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
