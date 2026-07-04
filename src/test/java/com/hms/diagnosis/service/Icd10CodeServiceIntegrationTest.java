package com.hms.diagnosis.service;

import com.hms.diagnosis.dto.Icd10CodeResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class Icd10CodeServiceIntegrationTest {

    @Autowired
    private Icd10CodeService icd10CodeService;

    @Test
    void gastroCodesReturnsAllTwentyFiveSeededCodes() {
        List<Icd10CodeResponse> codes = icd10CodeService.getGastroCodes();

        assertThat(codes).hasSize(25);
        assertThat(codes).extracting(Icd10CodeResponse::code).contains("K25.0", "K62.4", "K58.0");
    }

    @Test
    void searchMatchesByCode() {
        List<Icd10CodeResponse> results = icd10CodeService.search("K25");

        assertThat(results).extracting(Icd10CodeResponse::code).containsExactlyInAnyOrder("K25.0", "K25.9");
    }

    @Test
    void searchMatchesByDescriptionCaseInsensitive() {
        List<Icd10CodeResponse> results = icd10CodeService.search("crohn");

        assertThat(results).extracting(Icd10CodeResponse::code).containsExactlyInAnyOrder("K50.0", "K50.1");
    }

    @Test
    void searchMatchesIrritableBowelSyndrome() {
        List<Icd10CodeResponse> results = icd10CodeService.search("irritable");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).code()).isEqualTo("K58.0");
    }
}
