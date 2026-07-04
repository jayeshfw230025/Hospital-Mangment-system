package com.hms.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hms.auth.dto.LoginRequest;
import com.hms.auth.dto.OtpVerifyRequest;
import com.hms.auth.dto.RefreshTokenRequest;
import com.hms.auth.service.LoginOtpService;
import com.hms.patient.domain.Gender;
import com.hms.patient.dto.PatientRegistrationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthAndRbacIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LoginOtpService loginOtpService;

    @Autowired
    private ObjectMapper objectMapper;

    private String loginAndGetAccessToken(String username) throws Exception {
        return loginAndGetTokens(username)[0];
    }

    private String[] loginAndGetTokens(String username) throws Exception {
        String loginBody = objectMapper.writeValueAsString(new LoginRequest(username, "Passw0rd!23"));
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(loginBody))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String txnId = loginJson.at("/data/txnId").asText();
        String otp = loginOtpService.currentOtp(txnId);
        assertThat(otp).isNotNull();

        String verifyBody = objectMapper.writeValueAsString(new OtpVerifyRequest(txnId, otp));
        MvcResult verifyResult = mockMvc.perform(post("/api/v1/auth/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON).content(verifyBody))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode tokenJson = objectMapper.readTree(verifyResult.getResponse().getContentAsString());
        return new String[]{tokenJson.at("/data/accessToken").asText(), tokenJson.at("/data/refreshToken").asText()};
    }

    @Test
    void loginFlowIssuesValidAccessTokenAndMeReturnsProfile() throws Exception {
        String accessToken = loginAndGetAccessToken("doctor.demo");

        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("doctor.demo"))
                .andExpect(jsonPath("$.data.role").value("DOCTOR"));
    }

    @Test
    void requestWithoutTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/v1/patients/UPID-0000")).andExpect(status().isUnauthorized());
    }

    @Test
    void requestWithInvalidTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/v1/patients/UPID-0000").header("Authorization", "Bearer garbage.token.here"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void doctorCanAccessPatientsButPharmacistCannot() throws Exception {
        String doctorToken = loginAndGetAccessToken("doctor.demo");
        String pharmacistToken = loginAndGetAccessToken("pharmacist.demo");

        mockMvc.perform(get("/api/v1/patients/UPID-NOT-FOUND").header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/patients/UPID-NOT-FOUND").header("Authorization", "Bearer " + pharmacistToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void nurseCannotCreatePatient() throws Exception {
        String nurseToken = loginAndGetAccessToken("nurse.demo");

        mockMvc.perform(post("/api/v1/patients/register")
                        .header("Authorization", "Bearer " + nurseToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void onlyAdminCanAccessAuditTrail() throws Exception {
        String doctorToken = loginAndGetAccessToken("doctor.demo");
        String adminToken = loginAndGetAccessToken("admin.demo");

        mockMvc.perform(get("/api/v1/audit-trail").header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/audit-trail").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void auditTrailRecordsCreateAndViewActionsAfterApiCalls() throws Exception {
        String doctorToken = loginAndGetAccessToken("doctor.demo");
        String adminToken = loginAndGetAccessToken("admin.demo");

        PatientRegistrationRequest registration = new PatientRegistrationRequest(
                null, "Audit Trail Test Patient", LocalDate.of(1990, 1, 1), Gender.MALE, null, null,
                "Indian", null, null, null, "9101999001", null, null, null, null,
                null, null, null, null, null);

        MvcResult registerResult = mockMvc.perform(post("/api/v1/patients/register")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registration)))
                .andExpect(status().isCreated())
                .andReturn();
        String upid = objectMapper.readTree(registerResult.getResponse().getContentAsString())
                .at("/data/upid").asText();

        mockMvc.perform(get("/api/v1/patients/" + upid).header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk());

        MvcResult auditResult = mockMvc.perform(get("/api/v1/audit-trail/patient/" + upid + "?size=200")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode content = objectMapper.readTree(auditResult.getResponse().getContentAsString()).at("/data/content");
        java.util.List<String> actions = new java.util.ArrayList<>();
        content.forEach(node -> actions.add(node.get("action").asText()));

        assertThat(actions).contains("CREATE", "VIEW");
    }

    @Test
    void refreshTokenRotatesAndOldTokenCannotBeReused() throws Exception {
        String[] tokens = loginAndGetTokens("doctor.demo");
        String refreshToken = tokens[1];

        MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest(refreshToken))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(refreshResult.getResponse().getContentAsString());
        assertThat(json.at("/data/accessToken").asText()).isNotBlank();

        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest(refreshToken))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void logoutRevokesRefreshToken() throws Exception {
        String[] tokens = loginAndGetTokens("doctor.demo");
        String refreshToken = tokens[1];

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest(refreshToken))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest(refreshToken))))
                .andExpect(status().isBadRequest());
    }
}
