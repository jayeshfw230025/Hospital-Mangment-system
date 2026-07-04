package com.hms.ipd.service;

import com.hms.clinical.complaint.ComplaintType;
import com.hms.clinical.complaint.DurationUnit;
import com.hms.clinical.complaint.SeverityLevel;
import com.hms.clinical.complaint.TreatmentResponse;
import com.hms.ipd.dto.IpdComplaintRequest;
import com.hms.ipd.dto.IpdComplaintResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class IpdComplaintServiceIntegrationTest {

    @Autowired
    private IpdComplaintService ipdComplaintService;

    @Test
    void createsComplaintWithSeverityScoreAndTreatmentResponse() {
        IpdComplaintRequest request = new IpdComplaintRequest(
                201L,
                ComplaintType.HEMATEMESIS,
                SeverityLevel.SEVERE,
                8,
                6,
                DurationUnit.HOURS,
                "BP dropped to 90/60, tachycardia noted",
                TreatmentResponse.IMPROVED,
                "Responded to IV fluids",
                Map.of("amount", "~200ml")
        );

        IpdComplaintResponse response = ipdComplaintService.create(request);

        assertThat(response.id()).isNotNull();
        assertThat(response.severityScore()).isEqualTo(8);
        assertThat(response.responseToInitialTreatment()).isEqualTo(TreatmentResponse.IMPROVED);
        assertThat(response.complaintLabel()).isEqualTo("Hematemesis");
    }

    @Test
    void rejectsComplaintMissingRequiredDetailsForType() {
        IpdComplaintRequest request = new IpdComplaintRequest(
                202L, ComplaintType.BLOOD_IN_STOOL, SeverityLevel.MODERATE, 5, 1, DurationUnit.DAYS,
                null, TreatmentResponse.NOT_APPLICABLE, null, Map.of("color", "Bright red")
        );

        assertThatThrownBy(() -> ipdComplaintService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amount");
    }

    @Test
    void listsComplaintsForAdmission() {
        Long admissionId = 203L;
        ipdComplaintService.create(new IpdComplaintRequest(
                admissionId, ComplaintType.MELENA, SeverityLevel.MODERATE, 6, 1, DurationUnit.DAYS,
                null, TreatmentResponse.NO_CHANGE, null, Map.of("amount", "Moderate")));

        List<IpdComplaintResponse> results = ipdComplaintService.getByAdmissionId(admissionId);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).admissionId()).isEqualTo(admissionId);
    }
}
