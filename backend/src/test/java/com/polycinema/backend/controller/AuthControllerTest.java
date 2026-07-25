package com.polycinema.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polycinema.backend.entity.NguoiDung;
import com.polycinema.backend.repository.NguoiDungRepository;
import com.polycinema.backend.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for POST /api/auth/change-password  [RQ41]
 *
 * Uses MockMvc + mocked NguoiDungRepository so the real database is never hit.
 * JWT tokens are generated with the same JwtUtil the production code uses,
 * so the filter accepts them exactly as it would in production.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtUtil  jwtUtil;
    @Autowired BCryptPasswordEncoder encoder;
    @Autowired ObjectMapper objectMapper;

    // Mock the repository so no real DB call is made
    @MockBean NguoiDungRepository nguoiDungRepository;

    private static final String TEST_EMAIL    = "test_rq41@polycinema.com";
    private static final String CURRENT_PW    = "CurrentPass@123";
    private static final String NEW_PW        = "NewPass@456";
    private static final String WRONG_PW      = "WrongPass@000";

    private String validToken;
    private NguoiDung mockUser;

    @BeforeEach
    void setup() {
        // Reset mock invocations from previous tests or DataInitializer
        reset(nguoiDungRepository);
        
        // Generate a real JWT the filter will accept
        validToken = jwtUtil.generateToken(TEST_EMAIL, "USER");

        // Build a mock user with a known BCrypt hash
        mockUser = new NguoiDung();
        mockUser.setId(999L);
        mockUser.setEmail(TEST_EMAIL);
        mockUser.setMatKhauHash(encoder.encode(CURRENT_PW));
        mockUser.setHoTen("Test User RQ41");
        mockUser.setVaiTro("customer");
        mockUser.setTrangThai(true);
        mockUser.setIsEmailVerified(true);

        // Stub the repository look-up by email
        when(nguoiDungRepository.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.of(mockUser));
        // Stub save to return the same user
        when(nguoiDungRepository.save(any(NguoiDung.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    // ── TC-RQ41-01: correct current password → 200 OK ──────────────────────
    @Test
    @DisplayName("RQ41-01: change-password with correct currentPassword → 200 OK")
    void changePassword_correctCurrentPassword_returns200() throws Exception {
        Map<String, String> body = Map.of(
                "currentPassword", CURRENT_PW,
                "newPassword",     NEW_PW
        );

        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("thành công")));

        // Verify the user was saved with a new (different) hash
        verify(nguoiDungRepository, times(1)).save(argThat(u ->
                u.getEmail().equals(TEST_EMAIL)
                && encoder.matches(NEW_PW, u.getMatKhauHash())
        ));
    }

    // ── TC-RQ41-02: wrong current password → 400 Bad Request ───────────────
    @Test
    @DisplayName("RQ41-02: change-password with wrong currentPassword → 400 Bad Request")
    void changePassword_wrongCurrentPassword_returns400() throws Exception {
        Map<String, String> body = Map.of(
                "currentPassword", WRONG_PW,
                "newPassword",     NEW_PW
        );

        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("không đúng")));

        // Verify that save was NOT called — password must not be changed
        verify(nguoiDungRepository, never()).save(any());
    }

    // ── TC-RQ41-03: no Authorization header → 401 Unauthorized ────────────
    @Test
    @DisplayName("RQ41-03: change-password without Authorization header → 401")
    void changePassword_noAuthHeader_returns401() throws Exception {
        Map<String, String> body = Map.of(
                "currentPassword", CURRENT_PW,
                "newPassword",     NEW_PW
        );

        // /api/auth/** is in permitAll but the controller checks the principal manually
        // and returns 401 when no valid JWT is present
        mockMvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());

        verify(nguoiDungRepository, never()).save(any());
    }
}
