package com.hms.investigation.service;

import com.hms.common.exception.ResourceNotFoundException;
import com.hms.investigation.dto.InvestigationOrderRequest;
import com.hms.investigation.dto.InvestigationOrderResponse;
import com.hms.investigation.dto.InvestigationReportRequest;
import com.hms.investigation.dto.InvestigationReportResponse;
import com.hms.investigation.dto.ResultParameterRequest;
import com.hms.patient.domain.Gender;
import com.hms.patient.dto.PatientRegistrationRequest;
import com.hms.patient.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class InvestigationServiceIntegrationTest {

    @Autowired
    private InvestigationOrderService investigationOrderService;

    @Autowired
    private InvestigationReportService investigationReportService;

    @Autowired
    private PatientService patientService;

    private String registerPatient(String contactNumber) {
        PatientRegistrationRequest request = new PatientRegistrationRequest(
                null, "Investigation Test Patient", LocalDate.of(1982, 8, 8), Gender.MALE, null, null,
                "Indian", null, null, null, contactNumber, null, null, null, null,
                null, null, null, null, null
        );
        return patientService.register(request).upid();
    }

    @Test
    void createsOpdOrderForRegularInvestigation() {
        String patientId = registerPatient("9777700001");

        InvestigationOrderResponse response = investigationOrderService.createOrder(
                new InvestigationOrderRequest(patientId, 701L, null, "CBC", "Routine check"));

        assertThat(response.investigationName()).isEqualTo("Complete Blood Count (CBC)");
        assertThat(response.status().name()).isEqualTo("ORDERED");
    }

    @Test
    void rejectsIpdOnlyInvestigationWhenOrderedForOpdVisit() {
        String patientId = registerPatient("9777700002");

        assertThatThrownBy(() -> investigationOrderService.createOrder(
                new InvestigationOrderRequest(patientId, 702L, null, "ABG", null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void allowsIpdOnlyInvestigationForIpdAdmission() {
        String patientId = registerPatient("9777700003");

        InvestigationOrderResponse response = investigationOrderService.createOrder(
                new InvestigationOrderRequest(patientId, null, 801L, "ABG", null));

        assertThat(response.investigationName()).isEqualTo("ABG (Arterial Blood Gas)");
        assertThat(response.admissionId()).isEqualTo(801L);
    }

    @Test
    void rejectsOrderWithoutVisitOrAdmission() {
        String patientId = registerPatient("9777700004");

        assertThatThrownBy(() -> investigationOrderService.createOrder(
                new InvestigationOrderRequest(patientId, null, null, "CBC", null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsOrderForUnknownInvestigationType() {
        String patientId = registerPatient("9777700005");

        assertThatThrownBy(() -> investigationOrderService.createOrder(
                new InvestigationOrderRequest(patientId, 703L, null, "NOT_A_REAL_TEST", null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void submitsReportAndFlagsAbnormalValue() {
        String patientId = registerPatient("9777700006");
        InvestigationOrderResponse order = investigationOrderService.createOrder(
                new InvestigationOrderRequest(patientId, 704L, null, "HBA1C", null));

        InvestigationReportRequest reportRequest = new InvestigationReportRequest(
                order.id(), LocalDate.now(),
                List.of(new ResultParameterRequest("HbA1c", "9.2", "%", 4.0, 5.6, null)),
                "Poor glycemic control");

        InvestigationReportResponse report = investigationReportService.submit(reportRequest, null);

        assertThat(report.resultParameters().get(0).abnormal()).isTrue();
        assertThat(report.hasFile()).isFalse();

        List<InvestigationOrderResponse> refreshedOrders = investigationOrderService.getByPatientId(patientId);
        assertThat(refreshedOrders.get(0).status().name()).isEqualTo("COMPLETED");
        assertThat(refreshedOrders.get(0).latestReport()).isNotNull();
    }

    @Test
    void submitsReportWithFileAndDownloadsItBack() throws IOException {
        String patientId = registerPatient("9777700007");
        InvestigationOrderResponse order = investigationOrderService.createOrder(
                new InvestigationOrderRequest(patientId, 705L, null, "USG_ABDOMEN", null));

        MockMultipartFile file = new MockMultipartFile(
                "file", "usg-report.pdf", "application/pdf", "fake pdf content".getBytes(StandardCharsets.UTF_8));

        InvestigationReportRequest reportRequest = new InvestigationReportRequest(
                order.id(), LocalDate.now(),
                List.of(new ResultParameterRequest("Impression", "Mild hepatomegaly", null, null, null, true)),
                null);

        InvestigationReportResponse report = investigationReportService.submit(reportRequest, file);
        assertThat(report.hasFile()).isTrue();

        InvestigationReportService.DownloadableReport downloaded = investigationReportService.download(report.id());
        Resource resource = downloaded.resource();

        assertThat(downloaded.fileName()).isEqualTo("usg-report.pdf");
        assertThat(resource.getInputStream().readAllBytes())
                .isEqualTo("fake pdf content".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void tracksTrendAcrossThreeReportsForSameParameter() {
        String patientId = registerPatient("9777700008");

        String[] values = {"5.1", "6.3", "7.8"};
        for (String value : values) {
            InvestigationOrderResponse order = investigationOrderService.createOrder(
                    new InvestigationOrderRequest(patientId, 706L, null, "HBA1C", null));
            investigationReportService.submit(new InvestigationReportRequest(
                    order.id(), LocalDate.now(),
                    List.of(new ResultParameterRequest("HbA1c", value, "%", 4.0, 5.6, null)),
                    null), null);
        }

        List<InvestigationOrderResponse> orders = investigationOrderService.getByPatientId(patientId);
        InvestigationReportResponse latestReport = orders.get(0).latestReport();

        assertThat(latestReport.resultParameters().get(0).value()).isEqualTo("7.8");
        assertThat(latestReport.resultParameters().get(0).previousValues()).containsExactly("6.3", "5.1");
    }
}
