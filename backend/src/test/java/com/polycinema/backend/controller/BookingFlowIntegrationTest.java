package com.polycinema.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polycinema.backend.entity.DatVe;
import com.polycinema.backend.entity.LichChieu;
import com.polycinema.backend.entity.NguoiDung;
import com.polycinema.backend.service.AuthService;
import com.polycinema.backend.service.DatVeService;
import com.polycinema.backend.service.ThanhToanService;
import com.polycinema.backend.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * E2E System Integration Test for the Booking + Payment Flow.
 *
 * Verifies:
 * 1. Booking creation: POST /api/dat-ve
 * 2. VNPay URL generation: POST /api/thanh-toan/vnpay
 * 3. Payment callback processing: GET /api/thanh-toan/vnpay/callback
 * 4. QR Ticket retrieval: GET /api/ve/{maVe}/qr
 *
 * It uses MockMvc and mocks only the service layer to avoid external dependencies.
 */
@SpringBootTest
@AutoConfigureMockMvc
class BookingFlowIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtUtil jwtUtil;
    @Autowired ObjectMapper objectMapper;

    @MockBean AuthService authService;
    @MockBean DatVeService datVeService;
    @MockBean ThanhToanService thanhToanService;

    private static final String USER_EMAIL = "buyer@polycinema.com";
    private static final Long USER_ID = 101L;
    private String jwtToken;
    private NguoiDung mockUser;
    private DatVe mockBooking;

    @BeforeEach
    void setup() {
        jwtToken = jwtUtil.generateToken(USER_EMAIL, "USER");

        mockUser = new NguoiDung();
        mockUser.setId(USER_ID);
        mockUser.setEmail(USER_EMAIL);
        mockUser.setHoTen("Gia Bao");

        mockBooking = new DatVe();
        mockBooking.setId(500L);
        mockBooking.setMaDatVe("BK_INTEGRATION_99");
        mockBooking.setNguoiDung(mockUser);
        mockBooking.setTongTienThanhToan(new BigDecimal("120000"));
        mockBooking.setTrangThai("pending");
        mockBooking.setTrangThaiThanhToan("unpaid");

        LichChieu mockLich = new LichChieu();
        mockLich.setId(1L);
        mockBooking.setLichChieu(mockLich);

        // Stub auth — getUserIdFromToken extracts email from JWT → returns userId
        when(authService.getUserIdFromToken()).thenReturn(USER_ID);

        // Stub booking services
        when(datVeService.createBooking(eq(USER_ID), eq(1L), any(), any(), any(), any()))
                .thenReturn(mockBooking);
        when(datVeService.getBookingDetail(500L))
                .thenReturn(mockBooking);
        when(datVeService.findByMaDatVe("BK_INTEGRATION_99"))
                .thenReturn(mockBooking);
    }

    // ── E2E-01: Complete Booking Flow ──────────────────────────────────────
    @Test
    @DisplayName("E2E-01: Full flow: Create Booking → Generate Payment URL → Callback → Verify QR")
    void completeBookingFlow() throws Exception {
        // Step 1: Create booking
        Map<String, Object> bookingReq = Map.of(
                "lichChieuId", 1,
                "gheIds", List.of(10L, 11L),
                "comboData", List.of()
        );

        mockMvc.perform(post("/api/dat-ve")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(500))
                .andExpect(jsonPath("$.maDatVe").value("BK_INTEGRATION_99"))
                .andExpect(jsonPath("$.trangThai").value("pending"));

        // Step 2: Request VNPay checkout URL
        when(thanhToanService.createVNPayUrl(eq(500L), any(), any()))
                .thenReturn("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?mock=1");

        Map<String, Object> payReq = Map.of("datVeId", 500L);

        mockMvc.perform(post("/api/thanh-toan/vnpay")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentUrl").value("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?mock=1"));

        // Step 3: Simulate successful payment callback
        when(thanhToanService.handleVNPayCallback(any()))
                .thenAnswer(inv -> {
                    mockBooking.setTrangThai("confirmed");
                    mockBooking.setTrangThaiThanhToan("paid");
                    return true;
                });

        mockMvc.perform(get("/api/thanh-toan/vnpay/callback")
                        .param("vnp_ResponseCode", "00")
                        .param("vnp_TxnRef", "BK_INTEGRATION_99")
                        .param("vnp_SecureHash", "mockHash"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Step 4: Verify QR ticket generation PNG retrieval
        // (Only owners can fetch the QR ticket. Our mock user 101L matches the token)
        mockMvc.perform(get("/api/ve/BK_INTEGRATION_99/qr")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.startsWith(MediaType.IMAGE_PNG_VALUE)));
    }
}
