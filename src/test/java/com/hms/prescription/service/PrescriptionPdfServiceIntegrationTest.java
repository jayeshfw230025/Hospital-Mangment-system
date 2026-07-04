package com.hms.prescription.service;

import com.hms.patient.domain.Gender;
import com.hms.patient.dto.PatientRegistrationRequest;
import com.hms.patient.service.PatientService;
import com.hms.prescription.domain.Prescription;
import com.hms.prescription.dto.PrescriptionItemRequest;
import com.hms.prescription.dto.PrescriptionRequest;
import com.hms.prescription.dto.PrescriptionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class PrescriptionPdfServiceIntegrationTest {

    @Autowired
    private PrescriptionService prescriptionService;

    @Autowired
    private PrescriptionPdfService prescriptionPdfService;

    @Autowired
    private DrugService drugService;

    @Autowired
    private PatientService patientService;

    @Test
    void generatesValidPdfBytesAndStoresThemOnDisk() {
        PatientRegistrationRequest patientRequest = new PatientRegistrationRequest(
                null, "PDF Test Patient", LocalDate.of(1982, 4, 4), Gender.FEMALE, null, null,
                "Indian", null, null, null, "9899900001", null, null, null, null,
                null, null, null, null, null
        );
        String patientId = patientService.register(patientRequest).upid();

        Long omeprazoleId = drugService.search("Omeprazole").get(0).id();

        PrescriptionResponse created = prescriptionService.create(new PrescriptionRequest(
                patientId, 1001L, null, "Dr. Bose", "Dr. Bose - signed", null,
                List.of(new PrescriptionItemRequest(omeprazoleId, "20mg", "once daily", "Oral", 14, null, null, null))));

        Prescription entity = prescriptionService.getEntityById(created.id());
        PrescriptionPdfService.GeneratedPdf generated = prescriptionPdfService.generate(entity);

        assertThat(generated.bytes()).isNotEmpty();
        assertThat(new String(generated.bytes(), 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
        assertThat(generated.fileName()).isEqualTo("prescription-" + created.id() + ".pdf");
    }
}
