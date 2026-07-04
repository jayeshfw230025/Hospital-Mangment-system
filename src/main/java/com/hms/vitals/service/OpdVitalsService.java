package com.hms.vitals.service;

import com.hms.common.exception.ResourceNotFoundException;
import com.hms.patient.repository.PatientRepository;
import com.hms.vitals.domain.CoreVitals;
import com.hms.vitals.domain.OpdVitals;
import com.hms.vitals.domain.SourceType;
import com.hms.vitals.domain.TemperatureUnit;
import com.hms.vitals.domain.VitalAlert;
import com.hms.vitals.dto.OpdVitalsRequest;
import com.hms.vitals.dto.OpdVitalsResponse;
import com.hms.vitals.repository.OpdVitalsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OpdVitalsService {

    private final OpdVitalsRepository opdVitalsRepository;
    private final PatientRepository patientRepository;
    private final VitalsCalculator vitalsCalculator;
    private final VitalAlertEvaluator vitalAlertEvaluator;
    private final VitalAlertService vitalAlertService;

    public OpdVitalsService(OpdVitalsRepository opdVitalsRepository,
                             PatientRepository patientRepository,
                             VitalsCalculator vitalsCalculator,
                             VitalAlertEvaluator vitalAlertEvaluator,
                             VitalAlertService vitalAlertService) {
        this.opdVitalsRepository = opdVitalsRepository;
        this.patientRepository = patientRepository;
        this.vitalsCalculator = vitalsCalculator;
        this.vitalAlertEvaluator = vitalAlertEvaluator;
        this.vitalAlertService = vitalAlertService;
    }

    @Transactional
    public OpdVitalsResponse record(OpdVitalsRequest request) {
        if (patientRepository.findByUpid(request.patientId()).isEmpty()) {
            throw new ResourceNotFoundException("Patient not found with UPID: " + request.patientId());
        }

        TemperatureUnit unit = request.temperatureUnit() == null ? TemperatureUnit.CELSIUS : request.temperatureUnit();
        Double temperatureCelsius = vitalsCalculator.toCelsius(request.temperature(), unit);
        Double bmi = vitalsCalculator.calculateBmi(request.heightCm(), request.weightKg());

        CoreVitals coreVitals = new CoreVitals(
                request.systolicBp(), request.diastolicBp(), request.heartRate(), request.respiratoryRate(),
                temperatureCelsius, request.heightCm(), request.weightKg(), bmi,
                request.spo2(), request.painScore(), request.randomBloodSugar());

        OpdVitals vitals = new OpdVitals();
        vitals.setVisitId(request.visitId());
        vitals.setPatientUpid(request.patientId());
        vitals.setCoreVitals(coreVitals);

        OpdVitals saved = opdVitalsRepository.save(vitals);

        List<TriggeredAlert> triggered = vitalAlertEvaluator.evaluate(coreVitals, null);
        List<VitalAlert> raisedAlerts = vitalAlertService.raiseAlerts(
                saved.getPatientUpid(), SourceType.OPD, saved.getId(), triggered);

        return toResponse(saved, raisedAlerts.stream().map(vitalAlertService::toResponse).toList());
    }

    @Transactional(readOnly = true)
    public List<OpdVitalsResponse> getByVisitId(Long visitId) {
        return opdVitalsRepository.findByVisitIdOrderByCreatedAtAsc(visitId).stream()
                .map(v -> toResponse(v, vitalAlertService.getBySourceVitals(v.getId(), SourceType.OPD)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OpdVitalsResponse> getByPatientId(String patientId) {
        return opdVitalsRepository.findByPatientUpidOrderByCreatedAtAsc(patientId).stream()
                .map(v -> toResponse(v, vitalAlertService.getBySourceVitals(v.getId(), SourceType.OPD)))
                .toList();
    }

    private OpdVitalsResponse toResponse(OpdVitals vitals, List<com.hms.vitals.dto.VitalAlertResponse> alerts) {
        CoreVitals c = vitals.getCoreVitals();
        return new OpdVitalsResponse(
                vitals.getId(),
                vitals.getVisitId(),
                vitals.getPatientUpid(),
                c.getSystolicBp(),
                c.getDiastolicBp(),
                c.getHeartRate(),
                c.getRespiratoryRate(),
                c.getTemperatureCelsius(),
                vitalsCalculator.toFahrenheit(c.getTemperatureCelsius()),
                c.getHeightCm(),
                c.getWeightKg(),
                c.getBmi(),
                c.getSpo2(),
                c.getPainScore(),
                c.getRandomBloodSugar(),
                alerts,
                vitals.getCreatedAt()
        );
    }
}
