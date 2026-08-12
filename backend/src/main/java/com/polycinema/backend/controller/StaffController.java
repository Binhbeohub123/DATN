package com.polycinema.backend.controller;

import com.polycinema.backend.entity.ChiTietDatGhe;
import com.polycinema.backend.entity.ChiTietDatSanPham;
import com.polycinema.backend.entity.NguoiDung;
import com.polycinema.backend.entity.DatVe;
import com.polycinema.backend.entity.GheNgoi;
import com.polycinema.backend.entity.LichChieu;
import com.polycinema.backend.repository.ChiTietDatGheRepository;
import com.polycinema.backend.repository.DatVeRepository;
import com.polycinema.backend.repository.GheNgoiRepository;
import com.polycinema.backend.repository.LichChieuRepository;
import com.polycinema.backend.repository.NguoiDungRepository;
import com.polycinema.backend.service.DatVeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Staff-specific endpoints under /api/staff.
 * SecurityConfig already guards /api/staff/** with hasAnyRole('STAFF','ADMIN').
 *
 * Login is now handled by the shared POST /api/auth/login endpoint.
 * The frontend redirects STAFF-role users to /staff/dashboard after login.
 */
@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {

    private final NguoiDungRepository nguoiDungRepository;

    // Services for Phase 2
    private final LichChieuRepository lichChieuRepository;
    private final GheNgoiRepository gheNgoiRepository;
    private final ChiTietDatGheRepository chiTietDatGheRepository;
    private final DatVeService datVeService;

    // Services for Phase 3
    private final DatVeRepository datVeRepository;

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
     * GET /api/staff/pos/lich-chieu?rapChieuId={id}
     * All of today's showtimes for the given cinema, ordered by start time.
     * Lower bound is start of today (not "now") so past-but-today showtimes
     * remain visible for check-in and review. Date is always server-side — not client-controlled.
     */
    @GetMapping("/pos/lich-chieu")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<?> getPosLichChieu(
            @RequestParam(required = false) Long rapChieuId) {

        if (rapChieuId == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Thiếu tham số rapChieuId"));
        }

        java.time.LocalDateTime from = java.time.LocalDate.now().atStartOfDay();
        java.time.LocalDateTime to   = java.time.LocalDate.now().plusDays(1).atStartOfDay();

        List<LichChieu> list = lichChieuRepository.findTodayByRapChieuId(rapChieuId, from, to);
        return ResponseEntity.ok(list);
    }

    /**
     * GET /api/staff/pos/ghe/{lichChieuId}
     * Lấy sơ đồ ghế + trạng thái (available/booked) cho suất chiếu.
     */
    @GetMapping("/pos/ghe/{lichChieuId}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<?> getPosGhe(@PathVariable Long lichChieuId) {
        LichChieu lichChieu = lichChieuRepository.findById(lichChieuId).orElse(null);
        if (lichChieu == null) return ResponseEntity.notFound().build();

        Long phongChieuId = lichChieu.getPhongChieu().getId();
        List<GheNgoi> tatCaGhe = gheNgoiRepository.findByPhongChieuId(phongChieuId);

        Set<Long> gheDaDat = chiTietDatGheRepository.findByLichChieuId(lichChieuId)
                .stream().map(ct -> ct.getGheNgoi().getId()).collect(Collectors.toSet());

        record GheDTO(Long id, String hangGhe, Integer soGhe, String loaiGhe,
                      java.math.BigDecimal heSoGia, java.math.BigDecimal giaTien, String trangThai) {}

        List<GheDTO> response = tatCaGhe.stream()
                .map(g -> new GheDTO(
                        g.getId(), g.getHangGhe(), g.getSoGhe(), g.getLoaiGhe(),
                        g.getHeSoGia(),
                        lichChieu.getGiaCoBan().multiply(g.getHeSoGia()),
                        gheDaDat.contains(g.getId()) ? "booked" : "available"))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
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
            Object khachHangRaw = req.get("khachHangEmail");
            String khachHangEmail = khachHangRaw != null
                    ? khachHangRaw.toString().trim()
                    : null;

            DatVe datVe = datVeService.createCounterSale(
                    nhanVienId,
                    khachHangEmail,
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

            String maDatVe = req.get("maDatVe");
            if (maDatVe == null || maDatVe.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Mã đặt vé không được để trống"));
            }

            DatVe datVe = datVeRepository.findByMaDatVe(maDatVe).orElse(null);
            if (datVe == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Mã vé không hợp lệ"));
            }

            if ("đã sử dụng".equals(datVe.getTrangThaiCheckIn())) {
                Map<String, Object> err = new LinkedHashMap<>();
                err.put("message", "Vé đã được sử dụng lúc " +
                        (datVe.getThoiGianCheckIn() != null ? datVe.getThoiGianCheckIn().toString()
                                : "không rõ"));
                err.put("ticket", buildTicketInfo(datVe));
                return ResponseEntity.badRequest().body(err);
            }

            // Tùy chọn: Validate thời gian chiếu (không cho checkin nếu phim chưa chiếu
            // hoặc đã qua quá lâu)
            // Lấy thời gian bắt đầu
            if (datVe.getLichChieu() != null && datVe.getLichChieu().getThoiGianBatDau() != null) {
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                java.time.LocalDateTime start = datVe.getLichChieu().getThoiGianBatDau();

                // Cho phép check-in trước 20 phút và sau khi phim bắt đầu tối đa 120 phút
                if (now.isBefore(start.minusMinutes(20))) {
                    Map<String, Object> err = new LinkedHashMap<>();
                    err.put("message", "Chưa tới giờ check-in (chỉ hỗ trợ check-in trước 20 phút)");
                    err.put("ticket", buildTicketInfo(datVe));
                    return ResponseEntity.badRequest().body(err);
                }
                if (now.isAfter(start.plusMinutes(120))) {
                    Map<String, Object> err = new LinkedHashMap<>();
                    err.put("message", "Suất chiếu đã kết thúc hoặc quá giờ check-in");
                    err.put("ticket", buildTicketInfo(datVe));
                    return ResponseEntity.badRequest().body(err);
                }
            }

            datVe.setTrangThaiCheckIn("đã sử dụng");
            datVe.setThoiGianCheckIn(java.time.LocalDateTime.now());
            datVe.setNhanVienCheckIn(nhanVien);
            datVeRepository.save(datVe);

            // Xây dựng response
            Map<String, Object> res = new java.util.LinkedHashMap<>();
            res.put("message", "Check-in thành công");
            res.put("ticket", buildTicketInfo(datVe));

            return ResponseEntity.ok(res);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi check-in: " + e.getMessage()));
        }
    }

    private Map<String, Object> buildTicketInfo(DatVe datVe) {
        Map<String, Object> t = new LinkedHashMap<>();
        t.put("maDatVe", datVe.getMaDatVe());

        if (datVe.getLichChieu() != null) {
            LichChieu lc = datVe.getLichChieu();
            t.put("phim", lc.getPhim() != null ? lc.getPhim().getTenPhim() : "");
            t.put("thoiGianBatDau", lc.getThoiGianBatDau() != null ? lc.getThoiGianBatDau().toString() : null);
            if (lc.getPhongChieu() != null) {
                t.put("phongChieu", lc.getPhongChieu().getTenPhong() != null ? lc.getPhongChieu().getTenPhong() : "");
                t.put("rapChieu", lc.getPhongChieu().getRapChieu() != null && lc.getPhongChieu().getRapChieu().getTenRap() != null
                        ? lc.getPhongChieu().getRapChieu().getTenRap() : "");
            } else {
                t.put("phongChieu", "");
                t.put("rapChieu", "");
            }
        } else {
            t.put("phim", "");
            t.put("thoiGianBatDau", null);
            t.put("phongChieu", "");
            t.put("rapChieu", "");
        }

        List<String> ghe = new ArrayList<>();
        if (datVe.getChiTietDatGhe() != null) {
            for (ChiTietDatGhe ct : datVe.getChiTietDatGhe()) {
                if (ct.getGheNgoi() != null) {
                    String hang = ct.getGheNgoi().getHangGhe() != null ? ct.getGheNgoi().getHangGhe().trim() : "";
                    Integer so = ct.getGheNgoi().getSoGhe();
                    ghe.add(hang + (so != null ? so : ""));
                }
            }
        }
        ghe.sort(java.util.Comparator.naturalOrder());
        t.put("ghe", ghe);

        List<Map<String, Object>> combo = new ArrayList<>();
        if (datVe.getChiTietDatSanPham() != null) {
            for (ChiTietDatSanPham ct : datVe.getChiTietDatSanPham()) {
                Map<String, Object> c = new LinkedHashMap<>();
                c.put("tenSanPham", ct.getSanPham() != null ? ct.getSanPham().getTenSanPham() : "");
                c.put("soLuong", ct.getSoLuong());
                c.put("gia", ct.getGiaLucMua());
                combo.add(c);
            }
        }
        t.put("combo", combo);

        t.put("tongTienGoc", datVe.getTongTienGoc());
        t.put("tienGiamKhuyenMai", datVe.getTienGiamKhuyenMai());
        t.put("tienGiamTuDiem", datVe.getTienGiamTuDiem());
        t.put("diemSuDung", datVe.getDiemSuDung());
        t.put("tongTienThanhToan", datVe.getTongTienThanhToan());
        t.put("trangThai", datVe.getTrangThai());
        t.put("trangThaiThanhToan", datVe.getTrangThaiThanhToan());
        t.put("trangThaiCheckIn", datVe.getTrangThaiCheckIn());
        t.put("thoiGianCheckIn", datVe.getThoiGianCheckIn() != null ? datVe.getThoiGianCheckIn().toString() : null);

        boolean daSuDung = "đã sử dụng".equals(datVe.getTrangThaiCheckIn());
        t.put("daSuDung", daSuDung);

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime start = datVe.getLichChieu() != null ? datVe.getLichChieu().getThoiGianBatDau() : null;
        if (start != null) {
            java.time.LocalDateTime windowStart = start.minusMinutes(20);
            java.time.LocalDateTime windowEnd = start.plusMinutes(120);
            t.put("gioBatDauCheckIn", windowStart.toString());

            if (daSuDung) {
                t.put("canCheckIn", false);
                t.put("trangThaiQuet", "Vé đã được sử dụng");
            } else if (now.isBefore(windowStart)) {
                long mins = java.time.Duration.between(now, windowStart).toMinutes();
                t.put("canCheckIn", false);
                t.put("phutConLaiDenGioQuet", mins);
                t.put("trangThaiQuet", "Còn " + formatDuration(mins)
                        + " nữa mới được quét vé (bắt đầu từ " + formatTimeHm(windowStart) + ")");
            } else if (now.isBefore(start)) {
                long mins = java.time.Duration.between(now, start).toMinutes();
                t.put("canCheckIn", true);
                t.put("trangThaiQuet", "Có thể quét vé — phim chiếu sau " + formatDuration(mins));
            } else {
                long mins = java.time.Duration.between(start, now).toMinutes();
                boolean trongGio = now.isBefore(windowEnd);
                t.put("canCheckIn", trongGio);
                t.put("phutSuatDaChieu", mins);
                if (trongGio) {
                    t.put("trangThaiQuet", "Suất đã chiếu được " + formatDuration(mins)
                            + " — còn có thể quét vé");
                } else {
                    t.put("trangThaiQuet", "Suất đã diễn ra " + formatDuration(mins)
                            + " trước (quá giờ check-in)");
                }
            }
        } else {
            t.put("canCheckIn", true);
            t.put("trangThaiQuet", "");
        }

        if (datVe.getNguoiDung() != null) {
            NguoiDung u = datVe.getNguoiDung();
            t.put("khachHang", u.getHoTen() != null && !u.getHoTen().isBlank() ? u.getHoTen() : u.getEmail());
            t.put("email", u.getEmail());
        } else {
            t.put("khachHang", "Khách mua tại quầy");
            t.put("email", "");
        }

        if (datVe.getNhanVienCheckIn() != null) {
            NguoiDung nv = datVe.getNhanVienCheckIn();
            t.put("nhanVienCheckIn", nv.getHoTen() != null && !nv.getHoTen().isBlank() ? nv.getHoTen() : nv.getEmail());
        } else {
            t.put("nhanVienCheckIn", null);
        }

        return t;
    }

    private String formatDuration(long minutes) {
        if (minutes < 0) minutes = 0;
        long days = minutes / 1440;
        long hours = (minutes % 1440) / 60;
        long mins = minutes % 60;
        if (days > 0) return days + " ngày " + hours + " giờ " + mins + " phút";
        if (hours > 0) return hours + " giờ " + mins + " phút";
        return mins + " phút";
    }

    private String formatTimeHm(java.time.LocalDateTime t) {
        return String.format("%02d:%02d", t.getHour(), t.getMinute());
    }

    // ─────────────────────────────────────────────────────────────
    // Phase 3B: Báo cáo ca
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