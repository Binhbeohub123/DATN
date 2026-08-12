package com.polycinema.backend.service;

import com.polycinema.backend.entity.*;
import com.polycinema.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ThanhToanService.markPaid()  [RQ29]
 *
 * All repository and service dependencies are mocked with Mockito.
 * No Spring context is loaded — tests run in milliseconds.
 */
@ExtendWith(MockitoExtension.class)
class ThanhToanServiceTest {

    @Mock DatVeRepository       datVeRepository;
    @Mock ThanhToanRepository   thanhToanRepository;
    @Mock NguoiDungRepository   nguoiDungRepository;
    @Mock EmailService          emailService;
    @Mock DatVeService          datVeService;

    @InjectMocks ThanhToanService thanhToanService;

    private DatVe    testDatVe;
    private NguoiDung testUser;

    @BeforeEach
    void setup() {
        // ── User ────────────────────────────────────────────────
        testUser = new NguoiDung();
        testUser.setId(1L);
        testUser.setEmail("user_rq29@polycinema.com");
        testUser.setHoTen("Nguyen Van Test");
        testUser.setDiemTichLuy(0);
        testUser.setTongTienDaChi(BigDecimal.ZERO);
        testUser.setCapDoThanhVien("Thường");

        // ── Movie ───────────────────────────────────────────────
        Phim phim = new Phim();
        phim.setId(1L);
        phim.setTenPhim("Avengers: Doomsday");

        // ── Room ────────────────────────────────────────────────
        PhongChieu phong = new PhongChieu();
        phong.setId(1L);
        phong.setTenPhong("Phòng 1 — 2D");

        // ── Schedule ────────────────────────────────────────────
        LichChieu lichChieu = new LichChieu();
        lichChieu.setId(1L);
        lichChieu.setPhim(phim);
        lichChieu.setPhongChieu(phong);
        lichChieu.setThoiGianBatDau(LocalDateTime.of(2026, 7, 1, 19, 0));
        lichChieu.setThoiGianKetThuc(LocalDateTime.of(2026, 7, 1, 21, 40));
        lichChieu.setGiaCoBan(new BigDecimal("80000"));

        // ── Seat ────────────────────────────────────────────────
        GheNgoi ghe = new GheNgoi();
        ghe.setId(10L);
        ghe.setHangGhe("A ");
        ghe.setSoGhe(5);
        ghe.setHeSoGia(BigDecimal.ONE);

        ChiTietDatGhe ctdg = new ChiTietDatGhe();
        ctdg.setGheNgoi(ghe);
        ctdg.setGiaTien(new BigDecimal("80000"));

        // ── Booking ─────────────────────────────────────────────
        testDatVe = new DatVe();
        testDatVe.setId(1L);
        testDatVe.setMaDatVe("BK_TEST_001");
        testDatVe.setNguoiDung(testUser);
        testDatVe.setLichChieu(lichChieu);
        testDatVe.setChiTietDatGhe(new java.util.LinkedHashSet<>(java.util.List.of(ctdg)));
        testDatVe.setTongTienThanhToan(new BigDecimal("80000"));
        testDatVe.setTongTienGoc(new BigDecimal("80000"));
        testDatVe.setTienGiamKhuyenMai(BigDecimal.ZERO);
        testDatVe.setTienGiamTuDiem(BigDecimal.ZERO);
        testDatVe.setDiemSuDung(0);
        testDatVe.setTrangThai("pending");
        testDatVe.setTrangThaiThanhToan("unpaid");

        // Stub saves — return the argument unchanged
        when(datVeRepository.save(any(DatVe.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(thanhToanRepository.save(any(ThanhToan.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(nguoiDungRepository.save(any(NguoiDung.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    // ── TC-RQ29-01: sendBookingConfirmation invoked exactly once ────────────
    @Test
    @DisplayName("RQ29-01: markPaid() MUST invoke sendBookingConfirmation exactly once")
    void markPaid_callsEmailServiceOnce() {
        thanhToanService.markPaid(testDatVe, "VNPay", "TXN_12345");

        // Core assertion: email was sent exactly once with the correct DatVe
        verify(emailService, times(1)).sendBookingConfirmation(testDatVe);
    }

    // ── TC-RQ29-02: DatVe status set to confirmed + paid ────────────────────
    @Test
    @DisplayName("RQ29-02: markPaid() sets DatVe status to confirmed/paid")
    void markPaid_updatesBookingStatus() {
        thanhToanService.markPaid(testDatVe, "VNPay", "TXN_12345");

        assertThat(testDatVe.getTrangThai()).isEqualTo("confirmed");
        assertThat(testDatVe.getTrangThaiThanhToan()).isEqualTo("paid");
        verify(datVeRepository, atLeastOnce()).save(testDatVe);
    }

    // ── TC-RQ29-03: ThanhToan record is persisted ───────────────────────────
    @Test
    @DisplayName("RQ29-03: markPaid() saves a ThanhToan record with correct method and status")
    void markPaid_savesThanhToanRecord() {
        ArgumentCaptor<ThanhToan> captor = ArgumentCaptor.forClass(ThanhToan.class);
        thanhToanService.markPaid(testDatVe, "Momo", "MOMO_99");

        verify(thanhToanRepository).save(captor.capture());
        ThanhToan saved = captor.getValue();
        assertThat(saved.getPhuongThucThanhToan()).isEqualTo("Momo");
        assertThat(saved.getMaGiaoDich()).isEqualTo("MOMO_99");
        assertThat(saved.getTrangThai()).isEqualTo("success");
        assertThat(saved.getSoTien()).isEqualByComparingTo("80000");
    }

    // ── TC-RQ29-04: email failure is swallowed (non-fatal) ──────────────────
    @Test
    @DisplayName("RQ29-04: markPaid() does NOT throw when sendBookingConfirmation fails")
    void markPaid_emailFailure_doesNotAbortTransaction() {
        doThrow(new RuntimeException("SMTP unavailable"))
                .when(emailService).sendBookingConfirmation(any(DatVe.class));

        // Must not throw — email failure is caught silently
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->
                thanhToanService.markPaid(testDatVe, "VNPay", "TXN_ERR")
        );

        // Booking was still saved despite email failure
        verify(datVeRepository, atLeastOnce()).save(testDatVe);
        assertThat(testDatVe.getTrangThaiThanhToan()).isEqualTo("paid");
    }

    // ── TC-RQ29-05: loyalty points awarded correctly ─────────────────────────
    @Test
    @DisplayName("RQ29-05: markPaid() awards correct loyalty points (1 per 1000 VND, order >= 100k)")
    void markPaid_awardsLoyaltyPoints() {
        // 120 000 VND → 120 points (order >= 100k threshold)
        testDatVe.setTongTienThanhToan(new BigDecimal("120000"));
        testDatVe.setTongTienGoc(new BigDecimal("120000"));
        thanhToanService.markPaid(testDatVe, "VNPay", "TXN_PTS");
        assertThat(testUser.getDiemTichLuy()).isEqualTo(120);
    }

    // ── TC-RQ29-06: no points for orders under 100k ──────────────────────────
    @Test
    @DisplayName("RQ29-06: markPaid() awards 0 points when paid total < 100k")
    void markPaid_noPointsBelowThreshold() {
        // 80 000 VND < 100k → 0 points
        testDatVe.setTongTienThanhToan(new BigDecimal("80000"));
        testDatVe.setTongTienGoc(new BigDecimal("80000"));
        thanhToanService.markPaid(testDatVe, "VNPay", "TXN_SMALL");
        assertThat(testUser.getDiemTichLuy()).isEqualTo(0);
    }

    // ── TC-RQ29-07: only the actually-redeemed points are deducted ───────────
    @Test
    @DisplayName("RQ29-07: markPaid() deducts only the redeemed points, then awards earned")
    void markPaid_deductsOnlyActualPoints() {
        testUser.setDiemTichLuy(600);
        // Order 200k, used 600 pts (=60k discount, exactly 30% cap) → paid 140k
        testDatVe.setTongTienThanhToan(new BigDecimal("140000"));
        testDatVe.setTongTienGoc(new BigDecimal("200000"));
        testDatVe.setDiemSuDung(600);   // actual points stored on the booking
        testDatVe.setTienGiamTuDiem(new BigDecimal("60000"));
        thanhToanService.markPaid(testDatVe, "VNPay", "TXN_REDEEM");
        // +140 earned (140k paid), −600 redeemed → 600 + 140 − 600 = 140
        assertThat(testUser.getDiemTichLuy()).isEqualTo(140);
    }
}
