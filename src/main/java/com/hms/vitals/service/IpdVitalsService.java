package com.hms.vitals.service;

import com.hms.common.exception.ResourceNotFoundException;
import com.hms.patient.repository.PatientRepository;
import com.hms.vitals.domain.CoreVitals;
import com.hms.vitals.domain.IpdVitals;
import com.hms.vitals.domain.SourceType;
import com.hms.vitals.domain.TemperatureUnit;
import com.hms.vitals.domain.VitalAlert;
import com.hms.vitals.dto.IpdVitalsRequest;
import com.hms.vitals.dto.IpdVitalsResponse;
import com.hms.vitals.dto.VitalAlertResponse;
import com.hms.vitals.repository.IpdVitalsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class IpdVitalsService {

    private final IpdVitalsRepository ipdVitalsRepository;
    private final PatientRepository patientRepository;
    private final VitalsCalculator vitalsCalculator;
    private final VitalAlertEvaluator vitalAlertEvaluator;
    private final VitalAlertService vitalAlertService;

    public IpdVitalsService(IpdVitalsRepository ipdVitalsRepository,
                             PatientRepository patientRepository,
                             VitalsCalculator vitalsCalculator,
                             VitalAlertEvaluator vitalAlertEvaluator,
                             VitalAlertService vitalAlertService) {
        this.ipdVitalsRepository = ipdVitalsRepository;
        this.patientRepository = patientRepository;
        this.vitalsCalculator = vitalsCalculator;
        this.vitalAlertEvaluator = vitalAlertEvaluator;
        this.vitalAlertService = vitalAlertService;
    }

    @Transactional
    public IpdVitalsResponse record(IpdVitalsRequest request) {
        if (patientRepository.findByUpid(request.patientId()).isEmpty()) {
            throw new ResourceNotFoundException("Patient not found with UPID: " + request.patientId());
        }

        TemperatureUnit unit = request.temperatureUnit() == null ? TemperatureUnit.CELSIUS : request.temperatureUnit();
        Double temperatureCelsius = vitalsCalculator.toCelsius(request.temperature(), unit);
        Double bmi = vitalsCalculator.calculateBmi(request.heightCm(), request.weightKg());
        Double mapValue = vitalsCalculator.calculateMap(request.systolicBp(), request.diastolicBp());

        CoreVitals coreVitals = new CoreVitals(
                request.systolicBp(), request.diastolicBp(), request.heartRate(), request.respiratoryRate(),
                temperatureCelsius, request.heightCm(), request.weightKg(), bmi,
                request.spo2(), request.painScore(), request.randomBloodSugar());

        IpdVitals vitals = new IpdVitals();
        vitals.setAdmissionId(request.admissionId());
        vitals.setPatientUpid(request.patientId());
        vitals.setCoreVitals(coreVitals);
        vitals.setQtcMs(request.qtcMs());
        vitals.setMapValue(mapValue);
        vitals.setInputOutputBalanceMl(request.inputOutputBalanceMl());
        vitals.setGcsScore(request.gcsScore());
        vitals.setCvpCmH2o(request.cvpCmH2o());
        vitals.setGagReflex(request.gagReflex());

        IpdVitals saved = ipdVitalsRepository.save(vitals);

        List<TriggeredAlert> triggered = vitalAlertEvaluator.evaluate(coreVitals, request.gcsScore());
        List<VitalAlert> raisedAlerts = vitalAlertService.raiseAlerts(
                saved.getPatientUpid(), SourceType.IPD, saved.getId(), triggered);

        return toResponse(saved, raisedAlerts.stream().map(vitalAlertService::toResponse).toList());
    }

    @Transactional(readOnly = true)
    public List<IpdVitalsResponse> getByAdmissionId(Long admissionId) {
        return ipdVitalsRepository.findByAdmissionIdOrderByCreatedAtAsc(admissionId).stream()
                .map(v -> toResponse(v, vitalAlertService.getBySourceVitals(v.getId(), SourceType.IPD)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IpdVitalsResponse> getByPatientId(String patientId) {
        return ipdVitalsRepository.findByPatientUpidOrderByCreatedAtAsc(patientId).stream()
                .map(v -> toResponse(v, vitalAlertService.getBySourceVitals(v.getId(), SourceType.IPD)))
                .toList();
    }

    @Transactional(readOnly = true)
    public IpdVitalsResponse getById(Long id) {
        IpdVitals vitals = ipdVitalsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IPD vitals not found with id: " + id));
        return toResponse(vitals, vitalAlertService.getBySourceVitals(vitals.getId(), SourceType.IPD));
    }

    private IpdVitalsResponse toResponse(IpdVitals vitals, List<VitalAlertResponse> alerts) {
        CoreVitals c = vitals.getCoreVitals();
        return new IpdVitalsResponse(
                vitals.getId(),
                vitals.getAdmissionId(),
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
                vitals.getQtcMs(),
                vitals.getMapValue(),
                vitals.getInputOutputBalanceMl(),
                vitals.getGcsScore(),
                vitals.getCvpCmH2o(),
                vitals.getGagReflex(),
                alerts,
                vitals.getCreatedAt()
        );
    }
}
