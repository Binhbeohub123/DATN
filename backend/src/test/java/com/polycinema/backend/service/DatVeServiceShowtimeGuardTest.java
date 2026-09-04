package com.polycinema.backend.service;

import com.polycinema.backend.entity.*;
import com.polycinema.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

/**
 * Unit tests for the showtime sale-window guard (LỚP 1).
 *
 * Business rule: a showtime may be sold only up to 15 minutes AFTER it starts.
 *  - showtime < now => still within the 15-min grace => sale ALLOWED
 *  - showtime + 15m < now => deadline passed => sale BLOCKED (IllegalArgumentException)
 *  - showtime in the future => sale ALLOWED
 *
 * Applies to BOTH the customer flow (createBooking) and the Staff POS flow
 * (createCounterSale). Downstream seat/price mocks are only needed for the
 * ALLOWED cases, where execution continues past the guard.
 */
@ExtendWith(MockitoExtension.class)
class DatVeServiceShowtimeGuardTest {

    @Mock DatVeRepository             datVeRepository;
    @Mock ChiTietDatGheRepository     chiTietDatGheRepository;
    @Mock ChiTietDatSanPhamRepository chiTietDatSanPhamRepository;
    @Mock LichChieuRepository         lichChieuRepository;
    @Mock GheNgoiRepository           gheNgoiRepository;
    @Mock SanPhamRepository           sanPhamRepository;
    @Mock KhuyenMaiRepository         khuyenMaiRepository;
    @Mock NguoiDungRepository         nguoiDungRepository;
    @Mock SeatLockRepository          seatLockRepository;
    @Mock ThanhToanRepository         thanhToanRepository;
    @Mock EmailService                emailService;

    @InjectMocks DatVeService datVeService;

    private NguoiDung customer;
    private NguoiDung staff;
    private PhongChieu phong;
    private GheNgoi   ghe;
    private LichChieu lichChieu;

    @BeforeEach
    void setup() {
        customer = new NguoiDung();
        customer.setId(1L);
        customer.setEmail("buyer@test.com");
        customer.setHoTen("Khach");

        staff = new NguoiDung();
        staff.setId(2L);
        staff.setEmail("staff@test.com");
        staff.setHoTen("Nhan Vien");

        phong = new PhongChieu();
        phong.setId(10L);
        phong.setTenPhong("Phong 1");

        ghe = new GheNgoi();
        ghe.setId(100L);
        ghe.setHangGhe("A");
        ghe.setSoGhe(1);
        ghe.setHeSoGia(BigDecimal.ONE);
        ghe.setLoaiGhe("thường");
        ghe.setPhongChieu(phong);

        lichChieu = new LichChieu();
        lichChieu.setId(900L);
        lichChieu.setPhongChieu(phong);
        lichChieu.setGiaCoBan(new BigDecimal("80000"));

        // The showtime lookup happens BEFORE the guard — needed by every test.
        when(lichChieuRepository.findById(900L)).thenReturn(Optional.of(lichChieu));
    }

    // Stubs required once execution continues PAST the guard (allowed cases).
    // NOTA: the caller must also provide the user lookup (1L=customer, 2L=staff).
    void stubAllowedCommon() {
        when(gheNgoiRepository.findById(100L)).thenReturn(Optional.of(ghe));
        when(chiTietDatGheRepository.findActiveByLichChieuId(900L)).thenReturn(List.of());
        when(datVeRepository.save(any(DatVe.class))).thenAnswer(inv -> inv.getArgument(0));
        when(chiTietDatGheRepository.save(any(ChiTietDatGhe.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    void stubAllowedCustomerFlow() {
        stubAllowedCommon();
        when(nguoiDungRepository.findById(1L)).thenReturn(Optional.of(customer));
    }

    void stubAllowedStaffFlow() {
        stubAllowedCommon();
        when(nguoiDungRepository.findById(2L)).thenReturn(Optional.of(staff));
        when(thanhToanRepository.save(any(ThanhToan.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // ── Blocked: showtime started > 15 minutes ago ──────────────────────────
    @Nested
    @DisplayName("Showtime 20 minutes past start (past 15-min deadline)")
    class PastDeadline {

        @BeforeEach
        void past20() {
            lichChieu.setThoiGianBatDau(LocalDateTime.now().minusMinutes(20));
        }

        @Test
        @DisplayName("createBooking (customer) is blocked with clear message")
        void customerBlocked() {
            // No downstream stubs needed: the guard throws right after loading the showtime.
            assertThatThrownBy(() -> datVeService.createBooking(
                    1L, 900L, List.of(100L), List.of(), null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Suất chiếu đã kết thúc thời gian bán vé");
        }

        @Test
        @DisplayName("createCounterSale (Staff POS) is blocked with clear message")
        void staffBlocked() {
            assertThatThrownBy(() -> datVeService.createCounterSale(
                    2L, null, 900L, List.of(100L), List.of(), null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Suất chiếu đã kết thúc thời gian bán vé");
        }
    }

    // ── Allowed: showtime started 10 minutes ago (within the 15-min grace) ──
    @Nested
    @DisplayName("Showtime 10 minutes past start (within the 15-min grace)")
    class WithinGrace {

        @BeforeEach
        void tenPast() {
            lichChieu.setThoiGianBatDau(LocalDateTime.now().minusMinutes(10));
        }

        @Test
        @DisplayName("createBooking (customer) is still allowed")
        void customerAllowed() {
            stubAllowedCustomerFlow();

            DatVe result = datVeService.createBooking(
                    1L, 900L, List.of(100L), List.of(), null, null);

            assertThat(result).isNotNull();
            assertThat(result.getTrangThai()).isEqualTo("pending");
        }

        @Test
        @DisplayName("createCounterSale (Staff POS) is still allowed")
        void staffAllowed() {
            stubAllowedStaffFlow();

            DatVe result = datVeService.createCounterSale(
                    2L, null, 900L, List.of(100L), List.of(), null, null);

            assertThat(result).isNotNull();
            assertThat(result.getTrangThai()).isEqualTo("confirmed");
            assertThat(result.getTrangThaiThanhToan()).isEqualTo("paid");
        }
    }

    // ── Allowed: showtime in the future ─────────────────────────────────────
    @Nested
    @DisplayName("Showtime in the future")
    class FutureShowtime {

        @BeforeEach
        void future() {
            lichChieu.setThoiGianBatDau(LocalDateTime.now().plusHours(2));
        }

        @Test
        @DisplayName("createBooking (customer) is allowed")
        void customerAllowed() {
            stubAllowedCustomerFlow();

            DatVe result = datVeService.createBooking(
                    1L, 900L, List.of(100L), List.of(), null, null);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("createCounterSale (Staff POS) is allowed")
        void staffAllowed() {
            stubAllowedStaffFlow();

            DatVe result = datVeService.createCounterSale(
                    2L, null, 900L, List.of(100L), List.of(), null, null);

            assertThat(result).isNotNull();
        }
    }

    // ── POS: vé bán tại quầy phải gửi email cho người mua (kèm QR) ──
    @Nested
    @DisplayName("POS counter sale sends booking confirmation email")
    class PosEmail {

        @Test
        @DisplayName("createCounterSale with buyer email invokes sendBookingConfirmation")
        void posSendsEmail() {
            stubAllowedStaffFlow();
            when(nguoiDungRepository.findByEmail("buyer@test.com"))
                    .thenReturn(Optional.of(customer));

            DatVe result = datVeService.createCounterSale(
                    2L, "buyer@test.com", 900L, List.of(100L), List.of(), null, null);

            assertThat(result).isNotNull();
            verify(emailService, times(1)).sendBookingConfirmation(result);
        }

        @Test
        @DisplayName("createCounterSale without buyer email does NOT call sendBookingConfirmation")
        void posNoEmailNoSend() {
            stubAllowedStaffFlow();

            DatVe result = datVeService.createCounterSale(
                    2L, null, 900L, List.of(100L), List.of(), null, null);

            assertThat(result).isNotNull();
            verify(emailService, times(0)).sendBookingConfirmation(any());
        }
    }
}
