package com.hms.patient.service;

import com.hms.common.exception.DuplicateResourceException;
import com.hms.common.exception.ResourceNotFoundException;
import com.hms.patient.domain.Gender;
import com.hms.patient.dto.AbhaLinkInitiationResponse;
import com.hms.patient.dto.AddressDto;
import com.hms.patient.dto.GeoLocationDto;
import com.hms.patient.dto.PatientRegistrationRequest;
import com.hms.patient.dto.PatientResponse;
import com.hms.patient.dto.PatientUpdateRequest;
import com.hms.patient.dto.ReferralDetailsDto;
import com.hms.patient.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class PatientServiceIntegrationTest {

    @Autowired
    private PatientService patientService;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private QrCodeService qrCodeService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private OtpService otpService;

    private PatientRegistrationRequest sampleRequest(String contactNumber) {
        return new PatientRegistrationRequest(
                null,
                "Ramesh Kumar",
                LocalDate.of(1990, 5, 15),
                Gender.MALE,
                null,
                null,
                "Indian",
                null,
                "Farmer",
                "Graduate",
                contactNumber,
                null,
                "ramesh@example.com",
                new AddressDto("12 MG Road", null, "Pune", "Maharashtra", "Pune", "411001", "India"),
                new GeoLocationDto(new BigDecimal("18.5204"), new BigDecimal("73.8567")),
                "234567890123",
                null,
                null,
                null,
                null
        );
    }

    private PatientUpdateRequest sampleUpdateRequest(String contactNumber, String fullName) {
        return new PatientUpdateRequest(
                fullName,
                LocalDate.of(1990, 5, 15),
                Gender.MALE,
                null,
                null,
                "Indian",
                null,
                "Farmer",
                "Graduate",
                contactNumber,
                null,
                "ramesh@example.com",
                new AddressDto("12 MG Road", null, "Pune", "Maharashtra", "Pune", "411001", "India"),
                new GeoLocationDto(new BigDecimal("18.5204"), new BigDecimal("73.8567")),
                "234567890123",
                null,
                null,
                null,
                null
        );
    }

    @Test
    void registersPatientAndGeneratesUpid() {
        PatientResponse response = patientService.register(sampleRequest("9876543210"));

        assertThat(response.upid()).startsWith("UPID-");
        assertThat(response.fullName()).isEqualTo("Ramesh Kumar");
        assertThat(response.primaryContactNumber()).isEqualTo("9876543210");
    }

    @Test
    void encryptsAadhaarAtRest() {
        PatientResponse response = patientService.register(sampleRequest("9876543211"));

        String rawColumnValue = jdbcTemplate.queryForObject(
                "SELECT aadhaar_number FROM patients WHERE upid = ?", String.class, response.upid());
        String decryptedViaEntity = patientRepository.findByUpid(response.upid()).orElseThrow().getAadhaarNumber();

        assertThat(rawColumnValue).isNotEqualTo("234567890123");
        assertThat(decryptedViaEntity).isEqualTo("234567890123");
    }

    @Test
    void rejectsDuplicateRegistration() {
        PatientRegistrationRequest request = sampleRequest("9876543212");
        patientService.register(request);

        assertThatThrownBy(() -> patientService.register(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void generatesQrCodeForRegisteredPatient() {
        PatientResponse response = patientService.register(sampleRequest("9876543213"));

        String qr = patientService.getQrCode(response.upid());

        assertThat(qr).isNotEmpty();
    }

    @Test
    void updatesPatientDemographics() {
        PatientResponse registered = patientService.register(sampleRequest("9876543214"));

        PatientResponse updated = patientService.update(registered.upid(),
                sampleUpdateRequest("9876543214", "Ramesh Kumar Updated"));

        assertThat(updated.fullName()).isEqualTo("Ramesh Kumar Updated");
        assertThat(updated.upid()).isEqualTo(registered.upid());
    }

    @Test
    void rejectsUpdateWithContactNumberBelongingToAnotherPatient() {
        patientService.register(sampleRequest("9876543215"));
        PatientResponse second = patientService.register(sampleRequest("9876543216"));

        assertThatThrownBy(() -> patientService.update(second.upid(),
                sampleUpdateRequest("9876543215", "Someone Else")))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void searchFindsPatientByPartialName() {
        patientService.register(sampleRequest("9876543217"));

        Page<PatientResponse> results = patientService.search("ramesh", null, null, null, PageRequest.of(0, 10));

        assertThat(results.getContent()).extracting(PatientResponse::primaryContactNumber)
                .contains("9876543217");
    }

    @Test
    void completesAbhaLinkageAfterCorrectOtp() {
        PatientResponse registered = patientService.register(sampleRequest("9876543218"));

        AbhaLinkInitiationResponse initiation = patientService.initiateAbhaLink(registered.upid(), "12345678901234");
        String otp = otpService.currentOtp(initiation.txnId());

        PatientResponse linked = patientService.verifyOtpAndCompleteAbhaLink(initiation.txnId(), otp);

        assertThat(linked.abhaNumber()).isEqualTo("12345678901234");
    }

    @Test
    void rejectsAbhaLinkageWithIncorrectOtp() {
        PatientResponse registered = patientService.register(sampleRequest("9876543219"));

        AbhaLinkInitiationResponse initiation = patientService.initiateAbhaLink(registered.upid(), "12345678901234");

        assertThatThrownBy(() -> patientService.verifyOtpAndCompleteAbhaLink(initiation.txnId(), "000000"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void returnsReferralDetailsWhenPresent() {
        PatientRegistrationRequest request = new PatientRegistrationRequest(
                null, "Referred Patient", LocalDate.of(1985, 1, 1), Gender.FEMALE, null, null,
                "Indian", null, null, null, "9876543220", null, null, null, null,
                null, null, null, null,
                new com.hms.patient.dto.ReferralDetailsDto("Dr. Mehta", "City Hospital",
                        LocalDate.of(2026, 6, 1), "Cardiac evaluation", "9998887770")
        );
        PatientResponse registered = patientService.register(request);

        ReferralDetailsDto referral = patientService.getReferral(registered.upid());

        assertThat(referral.referringDoctorName()).isEqualTo("Dr. Mehta");
    }

    @Test
    void throwsWhenNoReferralDetailsPresent() {
        PatientResponse registered = patientService.register(sampleRequest("9876543221"));

        assertThatThrownBy(() -> patientService.getReferral(registered.upid()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
