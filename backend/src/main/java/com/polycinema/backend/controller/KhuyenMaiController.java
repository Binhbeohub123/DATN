package com.polycinema.backend.controller;

import com.polycinema.backend.entity.KhuyenMai;
import com.polycinema.backend.repository.KhuyenMaiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/khuyen-mai")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class KhuyenMaiController {

    private final KhuyenMaiRepository khuyenMaiRepository;

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
     * List all active promotions.
     */
    @GetMapping
    public ResponseEntity<List<KhuyenMai>> getAll() {
        return ResponseEntity.ok(khuyenMaiRepository.findByDangHoatDongTrue());
    }

    // ── ADMIN CRUD ──────────────────────────────────────────────

    /**
     * POST /api/khuyen-mai — ADMIN
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@RequestBody KhuyenMai km) {
        if (km.getMaKhuyenMai() != null) {
            km.setMaKhuyenMai(km.getMaKhuyenMai().trim().toUpperCase());
        }
        if (km.getDaSuDung() == null) km.setDaSuDung(0);
        if (km.getDangHoatDong() == null) km.setDangHoatDong(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(khuyenMaiRepository.save(km));
    }

    /**
     * PUT /api/khuyen-mai/{id} — ADMIN
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody KhuyenMai body) {
        KhuyenMai km = khuyenMaiRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khuyến mãi"));
        if (body.getTenKhuyenMai() != null) km.setTenKhuyenMai(body.getTenKhuyenMai());
        if (body.getMoTa() != null) km.setMoTa(body.getMoTa());
        if (body.getLoaiGiamGia() != null) km.setLoaiGiamGia(body.getLoaiGiamGia());
        if (body.getGiaTriGiam() != null) km.setGiaTriGiam(body.getGiaTriGiam());
        if (body.getGiaTriGiamToiDa() != null) km.setGiaTriGiamToiDa(body.getGiaTriGiamToiDa());
        if (body.getDonHangToiThieu() != null) km.setDonHangToiThieu(body.getDonHangToiThieu());
        if (body.getNgayBatDau() != null) km.setNgayBatDau(body.getNgayBatDau());
        if (body.getNgayKetThuc() != null) km.setNgayKetThuc(body.getNgayKetThuc());
        if (body.getGioiHanSuDung() != null) km.setGioiHanSuDung(body.getGioiHanSuDung());
        if (body.getDangHoatDong() != null) km.setDangHoatDong(body.getDangHoatDong());
        return ResponseEntity.ok(khuyenMaiRepository.save(km));
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
