package com.polycinema.backend.controller;

import com.polycinema.backend.entity.ChiTietDatGhe;
import com.polycinema.backend.entity.ChiTietDatSanPham;
import com.polycinema.backend.entity.DatVe;
import com.polycinema.backend.entity.GheNgoi;
import com.polycinema.backend.entity.LichChieu;
import com.polycinema.backend.entity.NguoiDung;
import com.polycinema.backend.repository.GheNgoiRepository;
import com.polycinema.backend.service.AuthService;
import com.polycinema.backend.service.DatVeService;
import com.polycinema.backend.service.LichChieuService;
import com.polycinema.backend.util.SeatDisplayUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@Slf4j
public class StaffController {

    private final AuthService authService;
    private final LichChieuService lichChieuService;
    private final DatVeService datVeService;
    private final GheNgoiRepository gheNgoiRepository;

    @PostMapping("/logout")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<?> staffLogout() {
        return ResponseEntity.ok(Map.of("message", "Đăng xuất thành công"));
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<?> getStaffProfile() {
        Long userId = authService.getUserIdFromToken();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Vui lòng đăng nhập"));
        }
        NguoiDung user = authService.getNguoiDungById(userId);
        if (user == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(user);
    }

    @GetMapping("/ping")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<?> ping() {
        return ResponseEntity.ok(Map.of(
                "message", "Staff endpoint accessible",
                "user", authService.getEmailFromToken()));
    }

    // ─────────────────────────────────────────────────────────────
    // Phase 2: Bán vé tại quầy (POS)
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/pos/lich-chieu")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<?> getPosLichChieu(@RequestParam(required = false) Long rapChieuId) {
        if (rapChieuId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Thiếu tham số rapChieuId"));
        }
        java.time.LocalDateTime from = java.time.LocalDateTime.now().minusMinutes(15);
        java.time.LocalDateTime to   = java.time.LocalDate.now().plusDays(1).atStartOfDay();
        List<LichChieu> list = lichChieuService.findPosByRapChieuId(rapChieuId, from, to);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/pos/ghe/{lichChieuId}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<?> getPosGhe(@PathVariable Long lichChieuId) {
        List<Map<String, Object>> result = lichChieuService.getGheTrong(lichChieuId);
        if (result.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/pos/dat-ve")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<?> createPosBooking(@RequestBody Map<String, Object> req) {
        try {
            Long nhanVienId = authService.getUserIdFromToken();
            if (nhanVienId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập");
            }

            Object lichChieuRaw = req.get("lichChieuId");
            if (lichChieuRaw == null) return ResponseEntity.badRequest().body("Thiếu trường lichChieuId");
            Long lichChieuId = ((Number) lichChieuRaw).longValue();

            @SuppressWarnings("unchecked")
            List<Long> gheIds = req.get("gheIds") == null ? java.util.Collections.emptyList()
                    : ((List<?>) req.get("gheIds")).stream()
                            .map(n -> n == null ? null : ((Number) n).longValue())
                            .collect(Collectors.toList());

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> comboData = (List<Map<String, Object>>) req.get("comboData");
            String maKhuyenMai = (String) req.get("maKhuyenMai");
            Integer diemSuDung = req.get("diemSuDung") != null
                    ? ((Number) req.get("diemSuDung")).intValue() : null;
            Object khachHangRaw = req.get("khachHangEmail");
            String khachHangEmail = khachHangRaw != null ? khachHangRaw.toString().trim() : null;

            DatVe datVe = datVeService.createCounterSale(
                    nhanVienId, khachHangEmail, lichChieuId,
                    gheIds, comboData, maKhuyenMai, diemSuDung);
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

    @PostMapping("/checkin")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<?> checkinQR(@RequestBody Map<String, String> req) {
        try {
            Long nhanVienId = authService.getUserIdFromToken();
            if (nhanVienId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập");
            }

            String maDatVe = req.get("maDatVe");
            if (maDatVe == null || maDatVe.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Mã đặt vé không được để trống"));
            }

            NguoiDung nhanVien = authService.getNguoiDungById(nhanVienId);
            DatVe datVe = datVeService.checkIn(maDatVe, nhanVien);

            Map<String, Object> res = new LinkedHashMap<>();
            res.put("message", "Check-in thành công");
            res.put("ticket", buildTicketInfo(datVe));
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("message", e.getMessage());
            try {
                DatVe dv = datVeService.findByMaDatVe(req.get("maDatVe"));
                if (dv != null) err.put("ticket", buildTicketInfo(dv));
            } catch (Exception ignored) {}
            return ResponseEntity.badRequest().body(err);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi check-in: " + e.getMessage()));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Phase 3B: Báo cáo ca
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/report/shift")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<?> getShiftReport(
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime from,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime to) {
        Long nhanVienId = authService.getUserIdFromToken();
        if (nhanVienId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập");
        }
        return ResponseEntity.ok(datVeService.getShiftReport(nhanVienId, from, to));
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────

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
            // Nhãn hiển thị: cần ĐỦ ghế cùng hàng của phòng suất chiếu
            Map<Long, Integer> nhanHienThi = new java.util.HashMap<>();
            try {
                if (datVe.getLichChieu() != null && datVe.getLichChieu().getPhongChieu() != null) {
                    Long phongId = datVe.getLichChieu().getPhongChieu().getId();
                    nhanHienThi = SeatDisplayUtil.buildRoomLabels(gheNgoiRepository.findByPhongChieuId(phongId));
                }
            } catch (Exception ex) {
                // fallback về soGhe vật lý bên dưới
            }
            for (ChiTietDatGhe ct : datVe.getChiTietDatGhe()) {
                if (ct.getGheNgoi() != null) {
                    String hang = ct.getGheNgoi().getHangGhe() != null ? ct.getGheNgoi().getHangGhe().trim() : "";
                    Integer so = ct.getGheNgoi().getSoGhe();
                    Integer hienThi = nhanHienThi.get(ct.getGheNgoi().getId());
                    boolean loiDi = ct.getGheNgoi().getLoaiGhe() != null
                            && "trống".equals(ct.getGheNgoi().getLoaiGhe().trim());
                    if (!loiDi && hienThi == null) {
                        log.warn("[soGheHienThi] SOT tai StaffController checkin: gheNgoiId={} hang={}{} — fallback soGhe vat ly",
                                ct.getGheNgoi().getId(), hang, so);
                    }
                    ghe.add(hang + (hienThi != null ? hienThi : (so != null ? so : "")));
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
                    t.put("trangThaiQuet", "Suất đã chiếu được " + formatDuration(mins) + " — còn có thể quét vé");
                } else {
                    t.put("trangThaiQuet", "Suất đã diễn ra " + formatDuration(mins) + " trước (quá giờ check-in)");
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
}
