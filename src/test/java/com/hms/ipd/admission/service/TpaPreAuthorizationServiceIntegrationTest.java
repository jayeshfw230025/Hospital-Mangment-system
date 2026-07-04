package com.hms.ipd.admission.service;

import com.hms.common.exception.ResourceNotFoundException;
import com.hms.ipd.admission.domain.AdmissionSource;
import com.hms.ipd.admission.domain.AdmissionType;
import com.hms.ipd.admission.domain.PreAuthStatus;
import com.hms.ipd.admission.dto.IpdAdmissionRequest;
import com.hms.ipd.admission.dto.IpdAdmissionResponse;
import com.hms.ipd.admission.dto.TpaPreAuthRequest;
import com.hms.ipd.admission.dto.TpaPreAuthResponse;
import com.hms.patient.domain.Gender;
import com.hms.patient.dto.PatientRegistrationRequest;
import com.hms.patient.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class TpaPreAuthorizationServiceIntegrationTest {

    @Autowired
    private TpaPreAuthorizationService tpaPreAuthorizationService;

    @Autowired
    private IpdAdmissionService ipdAdmissionService;

    @Autowired
    private PatientService patientService;

    private Long createAdmission(String contactNumber) {
        PatientRegistrationRequest patientRequest = new PatientRegistrationRequest(
                null, "TPA Test Patient", LocalDate.of(1988, 1, 1), Gender.MALE, null, null,
                "Indian", null, null, null, contactNumber, null, null, null, null,
                null, null, null, null, null
        );
        String patientId = patientService.register(patientRequest).upid();

        IpdAdmissionRequest admissionRequest = new IpdAdmissionRequest(
                patientId, AdmissionType.ELECTIVE, AdmissionSource.OPD,
                null, null, null, null, "K25.0", null, "Elective admission", "Signed");
        return ipdAdmissionService.create(admissionRequest, null).id();
    }

    @Test
    void submitsPreAuthDefaultingToPendingStatus() {
        Long admissionId = createAdmission("9101300001");

        TpaPreAuthResponse response = tpaPreAuthorizationService.submit(new TpaPreAuthRequest(
                admissionId, "Star Health Insurance", "POL123456", null, null, null, new BigDecimal("50000.00")));

        assertThat(response.approvalStatus()).isEqualTo(PreAuthStatus.PENDING);
        assertThat(response.estimatedCost()).isEqualByComparingTo("50000.00");
    }

    @Test
    void submitsPreAuthWithExplicitApprovalStatus() {
        Long admissionId = createAdmission("9101300002");

        TpaPreAuthResponse response = tpaPreAuthorizationService.submit(new TpaPreAuthRequest(
                admissionId, "HDFC Ergo", "POL987654", "PA-778899", LocalDate.now(),
                PreAuthStatus.APPROVED, new BigDecimal("75000.00")));

        assertThat(response.approvalStatus()).isEqualTo(PreAuthStatus.APPROVED);
        assertThat(response.preAuthNumber()).isEqualTo("PA-778899");
    }

    @Test
    void rejectsPreAuthForNonExistentAdmission() {
        assertThatThrownBy(() -> tpaPreAuthorizationService.submit(new TpaPreAuthRequest(
                999999L, "Star Health Insurance", "POL000000", null, null, null, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void latestPreAuthAppearsNestedInAdmissionResponse() {
        Long admissionId = createAdmission("9101300003");
        tpaPreAuthorizationService.submit(new TpaPreAuthRequest(
                admissionId, "Star Health Insurance", "POL111222", "PA-001", LocalDate.now(),
                PreAuthStatus.APPROVED, new BigDecimal("60000.00")));

        IpdAdmissionResponse admission = ipdAdmissionService.getById(admissionId);

        assertThat(admission.latestTpaPreAuth()).isNotNull();
        assertThat(admission.latestTpaPreAuth().approvalStatus()).isEqualTo(PreAuthStatus.APPROVED);
    }
}
