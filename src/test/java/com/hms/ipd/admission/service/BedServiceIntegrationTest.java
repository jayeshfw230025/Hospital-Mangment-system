package com.hms.ipd.admission.service;

import com.hms.ipd.admission.domain.AdmissionSource;
import com.hms.ipd.admission.domain.AdmissionType;
import com.hms.ipd.admission.domain.BedStatus;
import com.hms.ipd.admission.domain.WardType;
import com.hms.ipd.admission.dto.BedAllocateRequest;
import com.hms.ipd.admission.dto.BedResponse;
import com.hms.ipd.admission.dto.BedTransferRequest;
import com.hms.ipd.admission.dto.IpdAdmissionRequest;
import com.hms.ipd.admission.dto.IpdAdmissionResponse;
import com.hms.patient.domain.Gender;
import com.hms.patient.dto.PatientRegistrationRequest;
import com.hms.patient.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class BedServiceIntegrationTest {

    @Autowired
    private BedService bedService;

    @Autowired
    private IpdAdmissionService ipdAdmissionService;

    @Autowired
    private PatientService patientService;

    private String registerPatient(String contactNumber) {
        PatientRegistrationRequest request = new PatientRegistrationRequest(
                null, "Bed Test Patient", LocalDate.of(1975, 1, 1), Gender.FEMALE, null, null,
                "Indian", null, null, null, contactNumber, null, null, null, null,
                null, null, null, null, null
        );
        return patientService.register(request).upid();
    }

    private IpdAdmissionResponse createAdmission(String contactNumber) {
        String patientId = registerPatient(contactNumber);
        IpdAdmissionRequest request = new IpdAdmissionRequest(
                patientId, AdmissionType.EMERGENCY, AdmissionSource.ER,
                null, null, null, null, "K25.0", null, "Test admission", "Signed");
        return ipdAdmissionService.create(request, null);
    }

    private Long firstAvailableBedId(WardType wardType) {
        return bedService.getAvailableBeds(wardType).get(0).id();
    }

    @Test
    void allocatesBedAndMarksItOccupied() {
        IpdAdmissionResponse admission = createAdmission("9101200001");
        Long bedId = firstAvailableBedId(WardType.GENERAL);

        BedResponse response = bedService.allocate(new BedAllocateRequest(admission.id(), bedId));

        assertThat(response.status()).isEqualTo(BedStatus.OCCUPIED);
        assertThat(response.currentAdmissionId()).isEqualTo(admission.id());
    }

    @Test
    void rejectsAllocatingAnAlreadyOccupiedBed() {
        IpdAdmissionResponse admissionOne = createAdmission("9101200002");
        IpdAdmissionResponse admissionTwo = createAdmission("9101200003");
        Long bedId = firstAvailableBedId(WardType.ICU);

        bedService.allocate(new BedAllocateRequest(admissionOne.id(), bedId));

        assertThatThrownBy(() -> bedService.allocate(new BedAllocateRequest(admissionTwo.id(), bedId)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsAllocatingASecondBedToTheSameAdmission() {
        IpdAdmissionResponse admission = createAdmission("9101200004");
        Long firstBed = firstAvailableBedId(WardType.PRIVATE);
        bedService.allocate(new BedAllocateRequest(admission.id(), firstBed));

        Long secondBed = bedService.getAvailableBeds(WardType.PRIVATE).get(0).id();

        assertThatThrownBy(() -> bedService.allocate(new BedAllocateRequest(admission.id(), secondBed)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void transfersToNewBedAndFreesOldOne() {
        IpdAdmissionResponse admission = createAdmission("9101200005");
        Long firstBed = firstAvailableBedId(WardType.SEMI_PRIVATE);
        bedService.allocate(new BedAllocateRequest(admission.id(), firstBed));

        List<BedResponse> remaining = bedService.getAvailableBeds(WardType.SEMI_PRIVATE);
        Long secondBed = remaining.get(0).id();

        BedResponse transferred = bedService.transfer(new BedTransferRequest(admission.id(), secondBed, "Ward upgrade"));

        assertThat(transferred.id()).isEqualTo(secondBed);
        assertThat(transferred.status()).isEqualTo(BedStatus.OCCUPIED);

        boolean firstBedAvailableAgain = bedService.getAvailableBeds(WardType.SEMI_PRIVATE).stream()
                .anyMatch(b -> b.id().equals(firstBed));
        assertThat(firstBedAvailableAgain).isTrue();
    }

    @Test
    void availableBedsExcludeOccupiedOnes() {
        long availableBefore = bedService.getAvailableBeds(WardType.ICU).size();
        IpdAdmissionResponse admission = createAdmission("9101200006");
        Long bedId = firstAvailableBedId(WardType.ICU);
        bedService.allocate(new BedAllocateRequest(admission.id(), bedId));

        long availableAfter = bedService.getAvailableBeds(WardType.ICU).size();

        assertThat(availableAfter).isEqualTo(availableBefore - 1);
    }
}
