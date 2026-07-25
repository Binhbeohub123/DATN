package com.polycinema.backend.controller;

import com.polycinema.backend.entity.KhuyenMai;
import com.polycinema.backend.entity.Phim;
import com.polycinema.backend.repository.KhuyenMaiRepository;
import com.polycinema.backend.repository.PhimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/khuyen-mai")
@RequiredArgsConstructor
public class KhuyenMaiController {

    private final KhuyenMaiRepository khuyenMaiRepository;
    private final PhimRepository phimRepository;

    /**
     * POST /api/khuyen-mai/validate — authenticated
     * Validate a promo code and return discount info.
     * Body: { "maKhuyenMai": "CODE", "tongTien": 200000 }
     * Returns: { valid: true, discountAmount: BigDecimal, message: String, khuyenMai: KhuyenMai }
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validate(@RequestBody Map<String, Object> req) {
        String maKhuyenMai = (String) req.get("maKhuyenMai");
        if (maKhuyenMai == null || maKhuyenMai.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("valid", false, "message", "Mã khuyến mãi không được để trống"));
        }

        // Parse optional tongTien for discount calculation
        java.math.BigDecimal tongTien = java.math.BigDecimal.ZERO;
        Object tongTienRaw = req.get("tongTien");
        if (tongTienRaw != null) {
            try {
                tongTien = new java.math.BigDecimal(tongTienRaw.toString());
            } catch (NumberFormatException ignored) {}
        }

        KhuyenMai km = khuyenMaiRepository.findByMaKhuyenMai(maKhuyenMai.trim().toUpperCase())
                .orElse(null);

        if (km == null) {
            return ResponseEntity.ok(Map.of(
                    "valid", false,
                    "discountAmount", java.math.BigDecimal.ZERO,
                    "message", "Mã khuyến mãi không tồn tại"));
        }

        if (!Boolean.TRUE.equals(km.getDangHoatDong())) {
            return ResponseEntity.ok(Map.of(
                    "valid", false,
                    "discountAmount", java.math.BigDecimal.ZERO,
                    "message", "Mã khuyến mãi đã hết hiệu lực"));
        }

        LocalDate today = LocalDate.now();
        if (km.getNgayBatDau() != null && today.isBefore(km.getNgayBatDau())) {
            return ResponseEntity.ok(Map.of(
                    "valid", false,
                    "discountAmount", java.math.BigDecimal.ZERO,
                    "message", "Mã khuyến mãi chưa có hiệu lực"));
        }
        if (km.getNgayKetThuc() != null && today.isAfter(km.getNgayKetThuc())) {
            return ResponseEntity.ok(Map.of(
                    "valid", false,
                    "discountAmount", java.math.BigDecimal.ZERO,
                    "message", "Mã khuyến mãi đã hết hạn"));
        }

        if (km.getGioiHanSuDung() != null && km.getDaSuDung() != null
                && km.getDaSuDung() >= km.getGioiHanSuDung()) {
            return ResponseEntity.ok(Map.of(
                    "valid", false,
                    "discountAmount", java.math.BigDecimal.ZERO,
                    "message", "Mã khuyến mãi đã hết lượt sử dụng"));
        }

        // Check minimum order value
        if (km.getDonHangToiThieu() != null
                && tongTien.compareTo(km.getDonHangToiThieu()) < 0) {
            return ResponseEntity.ok(Map.of(
                    "valid", false,
                    "discountAmount", java.math.BigDecimal.ZERO,
                    "message", "Đơn hàng tối thiểu " + km.getDonHangToiThieu().toPlainString() + " VND"));
        }

        // Calculate actual discount amount
        java.math.BigDecimal discountAmount;
        if ("percent".equalsIgnoreCase(km.getLoaiGiamGia())) {
            discountAmount = tongTien.multiply(km.getGiaTriGiam())
                    .divide(java.math.BigDecimal.valueOf(100));
            if (km.getGiaTriGiamToiDa() != null) {
                discountAmount = discountAmount.min(km.getGiaTriGiamToiDa());
            }
        } else {
            // fixed
            discountAmount = km.getGiaTriGiam().min(tongTien);
        }

        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("valid", true);
        result.put("discountAmount", discountAmount);
        result.put("message", "Áp dụng thành công: " + km.getTenKhuyenMai());
        result.put("tenKhuyenMai", km.getTenKhuyenMai());
        result.put("loaiGiamGia", km.getLoaiGiamGia());
        result.put("giaTriGiam", km.getGiaTriGiam());
        result.put("giaTriGiamToiDa", km.getGiaTriGiamToiDa());
        result.put("donHangToiThieu", km.getDonHangToiThieu());
        // include full entity for backward-compat with bookingStore
        result.put("id", km.getId());
        result.put("maKhuyenMai", km.getMaKhuyenMai());
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/khuyen-mai — public
     * List all active promotions (customer-facing).
     */
    @GetMapping
    public ResponseEntity<List<KhuyenMai>> getAll() {
        return ResponseEntity.ok(khuyenMaiRepository.findByDangHoatDongTrue());
    }

    /**
     * GET /api/khuyen-mai/all — ADMIN
     * Returns ALL promotions (active and inactive) with their linked phims.
     * Used by the admin PromoPage to show the full list and pre-populate the edit form.
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<KhuyenMai>> getAllAdmin() {
        return ResponseEntity.ok(khuyenMaiRepository.findAllWithPhims());
    }

    /**
     * GET /api/khuyen-mai/active — public
     * Active promotions whose date range includes today, enriched with their phims list.
     * Returns: [ { id, maKhuyenMai, tenKhuyenMai, loaiGiamGia, giaTriGiam,
     *              ngayBatDau, ngayKetThuc, phims: [...] }, … ]
     */
    @GetMapping("/active")
    public ResponseEntity<List<KhuyenMai>> getActive() {
        return ResponseEntity.ok(khuyenMaiRepository.findActiveWithPhims(java.time.LocalDate.now()));
    }

    // ── ADMIN CRUD ──────────────────────────────────────────────

    /**
     * POST /api/khuyen-mai — ADMIN
     * Body: KhuyenMai fields + optional phimIds: [Long]
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        KhuyenMai km = new KhuyenMai();
        if (body.get("maKhuyenMai") != null)
            km.setMaKhuyenMai(((String) body.get("maKhuyenMai")).trim().toUpperCase());
        applyKhuyenMaiFields(km, body);
        if (km.getDaSuDung() == null) km.setDaSuDung(0);
        if (km.getDangHoatDong() == null) km.setDangHoatDong(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(khuyenMaiRepository.save(km));
    }

    /**
     * PUT /api/khuyen-mai/{id} — ADMIN
     * Body: any subset of KhuyenMai fields + optional phimIds: [Long]
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        KhuyenMai km = khuyenMaiRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khuyến mãi"));
        applyKhuyenMaiFields(km, body);
        return ResponseEntity.ok(khuyenMaiRepository.save(km));
    }

    /** Shared helper — applies fields from request body to a KhuyenMai entity. */
    @SuppressWarnings("unchecked")
    private void applyKhuyenMaiFields(KhuyenMai km, Map<String, Object> body) {
        if (body.containsKey("tenKhuyenMai")    && body.get("tenKhuyenMai")    != null) km.setTenKhuyenMai((String) body.get("tenKhuyenMai"));
        if (body.containsKey("moTa")            && body.get("moTa")            != null) km.setMoTa((String) body.get("moTa"));
        if (body.containsKey("loaiGiamGia")     && body.get("loaiGiamGia")     != null) km.setLoaiGiamGia((String) body.get("loaiGiamGia"));
        if (body.containsKey("giaTriGiam")      && body.get("giaTriGiam")      != null) km.setGiaTriGiam(new BigDecimal(body.get("giaTriGiam").toString()));
        if (body.containsKey("giaTriGiamToiDa") && body.get("giaTriGiamToiDa") != null) km.setGiaTriGiamToiDa(new BigDecimal(body.get("giaTriGiamToiDa").toString()));
        if (body.containsKey("donHangToiThieu") && body.get("donHangToiThieu") != null) km.setDonHangToiThieu(new BigDecimal(body.get("donHangToiThieu").toString()));
        if (body.containsKey("gioiHanSuDung")   && body.get("gioiHanSuDung")   != null) km.setGioiHanSuDung(((Number) body.get("gioiHanSuDung")).intValue());
        if (body.containsKey("ngayBatDau")      && body.get("ngayBatDau")      != null) km.setNgayBatDau(LocalDate.parse((String) body.get("ngayBatDau")));
        if (body.containsKey("ngayKetThuc")     && body.get("ngayKetThuc")     != null) km.setNgayKetThuc(LocalDate.parse((String) body.get("ngayKetThuc")));
        if (body.containsKey("dangHoatDong")    && body.get("dangHoatDong")    != null) km.setDangHoatDong((Boolean) body.get("dangHoatDong"));

        // Replace linked movies when phimIds is present (empty list = clear all / system-wide)
        if (body.containsKey("phimIds")) {
            List<?> rawIds = (List<?>) body.get("phimIds");
            List<Phim> phims;
            if (rawIds == null || rawIds.isEmpty()) {
                phims = new java.util.ArrayList<>();
            } else {
                phims = rawIds.stream()
                        .map(raw -> {
                            Long pid = ((Number) raw).longValue();
                            return phimRepository.findById(pid)
                                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phim id=" + pid));
                        })
                        .collect(Collectors.toList());
            }
            km.getPhims().clear();
            km.getPhims().addAll(phims);
        }
    }

    /**
     * DELETE /api/khuyen-mai/{id} — ADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        KhuyenMai km = khuyenMaiRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khuyến mãi"));
        km.setDangHoatDong(false);
        khuyenMaiRepository.save(km);
        return ResponseEntity.ok(Map.of("message", "Đã vô hiệu hóa khuyến mãi"));
    }
}
