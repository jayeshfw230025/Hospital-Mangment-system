package com.hms.patient.service;

import com.hms.patient.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Year;

@Service
public class UpidGeneratorService {

    private final PatientRepository patientRepository;
    private final String upidPrefix;

    public UpidGeneratorService(PatientRepository patientRepository,
                                 @Value("${hms.patient.upid-prefix:UPID}") String upidPrefix) {
        this.patientRepository = patientRepository;
        this.upidPrefix = upidPrefix;
    }

    public synchronized String generate() {
        String yearPrefix = upidPrefix + "-" + Year.now().getValue() + "-";
        long sequence = patientRepository.countByUpidStartingWith(yearPrefix) + 1;
        return yearPrefix + String.format("%06d", sequence);
    }
}
