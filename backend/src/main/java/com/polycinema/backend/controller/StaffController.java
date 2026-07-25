package com.polycinema.backend.controller;

import com.polycinema.backend.entity.NguoiDung;
import com.polycinema.backend.entity.DatVe;
import com.polycinema.backend.entity.LichChieu;
import com.polycinema.backend.repository.DatVeRepository;
import com.polycinema.backend.repository.LichChieuRepository;
import com.polycinema.backend.repository.NguoiDungRepository;
import com.polycinema.backend.service.AuthService;
import com.polycinema.backend.service.DatVeService;
import com.polycinema.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Staff-specific endpoints under /api/staff.
 * SecurityConfig already guards /api/staff/** with hasAnyRole('STAFF','ADMIN').
 *
 * Login approach chosen: (b) Separate POST /api/staff/login endpoint.
 * Reason: Staff login needs to reject non-staff accounts with a clear 403
 * message,
 * and the frontend staff UI routes to a dedicated /staff/login page.
 * The endpoint delegates to AuthService.login() for credential validation,
 * then additionally verifies the user has VaiTro = 'staff' or 'admin'.
 */
@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {

    private final AuthService authService;
    private final NguoiDungRepository nguoiDungRepository;
    private final JwtUtil jwtUtil;

    // Services for Phase 2
    private final LichChieuRepository lichChieuRepository;
    private final LichChieuController lichChieuController; // For reusing seat map logic
    private final DatVeService datVeService;

    // Services for Phase 3
    private final DatVeRepository datVeRepository;

    /**
     * POST /api/staff/login
     * Body: { "email": "...", "password": "..." }
     * Returns JWT token if credentials valid AND user has staff/admin role.
     * Returns 403 if account is not staff.
     */
    @PostMapping("/login")
    public ResponseEntity<?> staffLogin(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String password = req.get("password");

        // Delegate credential validation to existing AuthService
        String result = authService.login(email, password);

        // AuthService returns error messages for bad credentials
        if (result.equals("Email không được để trống")
                || result.equals("Mật khẩu không được để trống")) {
            return ResponseEntity.badRequest().body(Map.of("message", result));
        }

        if (result.equals("Sai email hoặc mật khẩu")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", result));
        }

        if (result.equals("Tài khoản đã bị khóa")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", result));
        }

        if (result.equals("Email chưa xác thực")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", result));
        }

        // At this point, result is a valid JWT token.
        // Verify the user actually has staff or admin role.
        String role = jwtUtil.extractRole(result);
        if (!"STAFF".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Tài khoản không có quyền nhân viên"));
        }

        return ResponseEntity.ok(Map.of("token", result));
    }

    /**
     * POST /api/staff/logout
     * Staff logout — client-side token removal.
     * (JWT is stateless; this endpoint exists for API completeness and future
     * token blacklisting if needed.)
     */
    @PostMapping("/logout")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<?> staffLogout() {
        // Stateless JWT — logout is handled client-side by clearing localStorage.
        // This endpoint confirms the action and can be extended with token
        // blacklisting.
        return ResponseEntity.ok(Map.of("message", "Đăng xuất thành công"));
    }

    /**
     * GET /api/staff/profile
     * Returns staff profile info. Proves role-based guard works end-to-end.
     */
    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<?> getStaffProfile() {
        String email = getEmailFromToken();
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Vui lòng đăng nhập"));
        }

        NguoiDung user = nguoiDungRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        user.setMatKhauHash(null);
        return ResponseEntity.ok(user);
    }

    /**
     * GET /api/staff/ping
     * Placeholder endpoint to verify staff role-based authorization works
     * end-to-end.
     */
    @GetMapping("/ping")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<?> ping() {
        return ResponseEntity.ok(Map.of(
                "message", "Staff endpoint accessible",
                "user", getEmailFromToken()));
    }

    // ─────────────────────────────────────────────────────────────
    // Phase 2: Bán vé tại quầy (POS)
    // ─────────────────────────────────────────────────────────────

    /**
     * GET /api/staff/pos/lich-chieu
     * Danh sách suất chiếu sắp tới.
     */
    @GetMapping("/pos/lich-chieu")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<?> getPosLichChieu(
            @RequestParam(required = false) Long phimId,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate ngay) {

        // Tái sử dụng logic của LichChieuController
        return lichChieuController.getLichChieu(phimId, ngay);
    }

    /**
     * GET /api/staff/pos/ghe/{lichChieuId}
     * Lấy sơ đồ ghế + trạng thái.
     */
    @GetMapping("/pos/ghe/{lichChieuId}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<?> getPosGhe(@PathVariable Long lichChieuId) {
        // Tái sử dụng logic của LichChieuController
        return lichChieuController.getGheTrong(lichChieuId);
    }

    /**
     * POST /api/staff/pos/dat-ve
     * Tạo đơn đặt vé tại quầy, thanh toán tiền mặt.
     */
    @PostMapping("/pos/dat-ve")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<?> createPosBooking(@RequestBody Map<String, Object> req) {
        try {
            Long nhanVienId = getUserIdFromToken();
            if (nhanVienId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập");
            }

            Object lichChieuRaw = req.get("lichChieuId");
            if (lichChieuRaw == null) {
                return ResponseEntity.badRequest().body("Thiếu trường lichChieuId");
            }
            Long lichChieuId = ((Number) lichChieuRaw).longValue();

            @SuppressWarnings("unchecked")
            List<Long> gheIds = req.get("gheIds") == null ? java.util.Collections.emptyList()
                    : ((List<?>) req.get("gheIds")).stream()
                            .map(n -> {
                                if (n == null) {
                                    throw new IllegalArgumentException("Danh sách gheIds có phần tử null");
                                }
                                return ((Number) n).longValue();
                            })
                            .collect(java.util.stream.Collectors.toList());

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> comboData = (List<Map<String, Object>>) req.get("comboData");
            String maKhuyenMai = (String) req.get("maKhuyenMai");
            Integer diemSuDung = req.get("diemSuDung") != null
                    ? ((Number) req.get("diemSuDung")).intValue()
                    : null;
            Object khachHangRaw = req.get("khachHangId");
            Long khachHangId = khachHangRaw != null
                    ? ((Number) khachHangRaw).longValue()
                    : null;

            DatVe datVe = datVeService.createCounterSale(
                    nhanVienId,
                    khachHangId,
                    lichChieuId,
                    gheIds,
                    comboData,
                    maKhuyenMai,
                    diemSuDung);

            return ResponseEntity.status(HttpStatus.CREATED).body(datVe);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi tạo đơn đặt vé: " + e.getMessage()));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Phase 3A: Quét QR Check-in
    // ─────────────────────────────────────────────────────────────

    /**
     * POST /api/staff/checkin
     * Body: { "maQR": "..." }
     */
    @PostMapping("/checkin")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<?> checkinQR(@RequestBody Map<String, String> req) {
        try {
            Long nhanVienId = getUserIdFromToken();
            if (nhanVienId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập");
            }
            NguoiDung nhanVien = nguoiDungRepository.findById(nhanVienId).orElse(null);

            String maQR = req.get("maQR");
            if (maQR == null || maQR.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Mã QR không được để trống"));
            }

            DatVe datVe = datVeRepository.findByMaQR(maQR).orElse(null);
            if (datVe == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Mã vé không hợp lệ"));
            }

            if ("đã sử dụng".equals(datVe.getTrangThaiCheckIn())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Vé đã được sử dụng lúc " +
                                (datVe.getThoiGianCheckIn() != null ? datVe.getThoiGianCheckIn().toString()
                                        : "không rõ")));
            }

            // Tùy chọn: Validate thời gian chiếu (không cho checkin nếu phim chưa chiếu
            // hoặc đã qua quá lâu)
            // Lấy thời gian bắt đầu
            if (datVe.getLichChieu() != null && datVe.getLichChieu().getThoiGianBatDau() != null) {
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                java.time.LocalDateTime start = datVe.getLichChieu().getThoiGianBatDau();

                // Cho phép check-in trước 60 phút và sau khi phim bắt đầu tối đa 60 phút
                if (now.isBefore(start.minusMinutes(60))) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("message", "Chưa tới giờ check-in (chỉ hỗ trợ check-in trước 60 phút)"));
                }
                if (now.isAfter(start.plusMinutes(120))) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("message", "Suất chiếu đã kết thúc hoặc quá giờ check-in"));
                }
            }

            datVe.setTrangThaiCheckIn("đã sử dụng");
            datVe.setThoiGianCheckIn(java.time.LocalDateTime.now());
            datVe.setNhanVienCheckIn(nhanVien);
            datVeRepository.save(datVe);

            // Xây dựng response
            Map<String, Object> res = new java.util.LinkedHashMap<>();
            res.put("message", "Check-in thành công");
            res.put("maDatVe", datVe.getMaDatVe());
            if (datVe.getLichChieu() != null) {
                res.put("phim",
                        datVe.getLichChieu().getPhim() != null ? datVe.getLichChieu().getPhim().getTenPhim() : "");
                res.put("suatChieu", datVe.getLichChieu().getThoiGianBatDau().toString());
            }
            if (datVe.getNguoiDung() != null) {
                res.put("khachHang", datVe.getNguoiDung().getHoTen() != null ? datVe.getNguoiDung().getHoTen()
                        : datVe.getNguoiDung().getEmail());
            } else {
                res.put("khachHang", "Khách mua tại quầy");
            }

            return ResponseEntity.ok(res);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi check-in: " + e.getMessage()));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Phase 3B: Báo cáo ca
    // ─────────────────────────────────────────────────────────────

    /**
     * GET /api/staff/report/shift?from=...&to=...
     */
    @GetMapping("/report/shift")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<?> getShiftReport(
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime from,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime to) {

        Long nhanVienId = getUserIdFromToken();
        if (nhanVienId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập");
        }

        java.time.LocalDateTime start = from != null ? from : java.time.LocalDate.now().atStartOfDay();
        java.time.LocalDateTime end = to != null ? to : java.time.LocalDateTime.now();

        // Query các DatVe có nhanVienId = nhanVienId và ngayTao trong khoảng [start,
        // end]
        // Vì repository không có sẵn hàm nên dùng findAll rồi filter
        List<DatVe> allDatVe = datVeRepository.findAll();

        List<DatVe> shiftSales = allDatVe.stream()
                .filter(dv -> dv.getNhanVien() != null && dv.getNhanVien().getId().equals(nhanVienId))
                .filter(dv -> dv.getNgayTao() != null && !dv.getNgayTao().isBefore(start)
                        && !dv.getNgayTao().isAfter(end))
                .filter(dv -> "paid".equals(dv.getTrangThaiThanhToan()))
                .collect(java.util.stream.Collectors.toList());

        long tongVeBan = shiftSales.size();
        java.math.BigDecimal tongDoanhThu = shiftSales.stream()
                .map(dv -> dv.getTongTienThanhToan() != null ? dv.getTongTienThanhToan() : java.math.BigDecimal.ZERO)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        // Check-in trong ca
        long soVeCheckIn = allDatVe.stream()
                .filter(dv -> dv.getNhanVienCheckIn() != null && dv.getNhanVienCheckIn().getId().equals(nhanVienId))
                .filter(dv -> dv.getThoiGianCheckIn() != null && !dv.getThoiGianCheckIn().isBefore(start)
                        && !dv.getThoiGianCheckIn().isAfter(end))
                .count();

        Map<String, Object> stats = new java.util.LinkedHashMap<>();
        stats.put("from", start.toString());
        stats.put("to", end.toString());
        stats.put("tongVeBan", tongVeBan);
        stats.put("tongDoanhThu", tongDoanhThu);
        stats.put("soVeCheckIn", soVeCheckIn);

        return ResponseEntity.ok(stats);
    }

    // ─────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────

    private Long getUserIdFromToken() {
        String email = getEmailFromToken();
        if (email != null) {
            return nguoiDungRepository.findByEmail(email).map(NguoiDung::getId).orElse(null);
        }
        return null;
    }

    private String getEmailFromToken() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()
                    && !"anonymousUser".equals(auth.getPrincipal())) {
                Object principal = auth.getPrincipal();
                if (principal instanceof String) {
                    return principal.toString();
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}