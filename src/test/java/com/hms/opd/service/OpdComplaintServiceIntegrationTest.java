package com.hms.opd.service;

import com.hms.clinical.complaint.ComplaintType;
import com.hms.clinical.complaint.DurationUnit;
import com.hms.clinical.complaint.FrequencyLevel;
import com.hms.clinical.complaint.SeverityLevel;
import com.hms.opd.dto.OpdComplaintRequest;
import com.hms.opd.dto.OpdComplaintResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class OpdComplaintServiceIntegrationTest {

    @Autowired
    private OpdComplaintService opdComplaintService;

    @Test
    void createsAbdominalPainComplaintWithRequiredDetails() {
        OpdComplaintRequest request = new OpdComplaintRequest(
                101L,
                ComplaintType.ABDOMINAL_PAIN,
                SeverityLevel.MODERATE,
                3,
                DurationUnit.DAYS,
                FrequencyLevel.FREQUENT,
                LocalDate.now().minusDays(3),
                "Patient reports cramping pain",
                Map.of("location", "Epigastric", "aggravatingFactors", "Spicy food", "relievingFactors", "Antacids")
        );

        OpdComplaintResponse response = opdComplaintService.create(request);

        assertThat(response.id()).isNotNull();
        assertThat(response.complaintLabel()).isEqualTo("Abdominal Pain");
        assertThat(response.details()).containsEntry("location", "Epigastric");
    }

    @Test
    void rejectsAbdominalPainComplaintMissingRequiredDetails() {
        OpdComplaintRequest request = new OpdComplaintRequest(
                102L, ComplaintType.ABDOMINAL_PAIN, SeverityLevel.MILD, 1, DurationUnit.DAYS,
                FrequencyLevel.OCCASIONAL, LocalDate.now(), null,
                Map.of("location", "Epigastric")
        );

        assertThatThrownBy(() -> opdComplaintService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("aggravatingFactors");
    }

    @Test
    void allowsComplaintTypeWithNoRequiredDetailsAndEmptyMap() {
        OpdComplaintRequest request = new OpdComplaintRequest(
                103L, ComplaintType.BLOATING, SeverityLevel.MILD, 2, DurationUnit.WEEKS,
                FrequencyLevel.RARE, LocalDate.now(), null, null
        );

        OpdComplaintResponse response = opdComplaintService.create(request);

        assertThat(response.id()).isNotNull();
    }

    @Test
    void listsComplaintsForVisitInCreationOrder() {
        Long visitId = 104L;
        opdComplaintService.create(new OpdComplaintRequest(
                visitId, ComplaintType.FEVER, SeverityLevel.MODERATE, 2, DurationUnit.DAYS, null,
                LocalDate.now(), null, Map.of("temperature", "101F", "pattern", "Intermittent")));
        opdComplaintService.create(new OpdComplaintRequest(
                visitId, ComplaintType.FATIGUE, SeverityLevel.MILD, 2, DurationUnit.DAYS, null,
                LocalDate.now(), null, Map.of("dailyActivityImpact", "Reduced work capacity")));

        List<OpdComplaintResponse> results = opdComplaintService.getByVisitId(visitId);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).complaintType()).isEqualTo(ComplaintType.FEVER);
        assertThat(results.get(1).complaintType()).isEqualTo(ComplaintType.FATIGUE);
    }

    @Test
    void updatesExistingComplaint() {
        OpdComplaintResponse created = opdComplaintService.create(new OpdComplaintRequest(
                105L, ComplaintType.JAUNDICE, SeverityLevel.MILD, 5, DurationUnit.DAYS, null,
                LocalDate.now(), null, Map.of("pruritus", "false")));

        OpdComplaintResponse updated = opdComplaintService.update(created.id(), new OpdComplaintRequest(
                105L, ComplaintType.JAUNDICE, SeverityLevel.SEVERE, 7, DurationUnit.DAYS, null,
                LocalDate.now(), "Worsened over the week", Map.of("pruritus", "true")));

        assertThat(updated.severity()).isEqualTo(SeverityLevel.SEVERE);
        assertThat(updated.durationValue()).isEqualTo(7);
        assertThat(updated.details()).containsEntry("pruritus", "true");
    }
}
