package com.polycinema.backend.controller;

import com.polycinema.backend.entity.LichChieu;
import com.polycinema.backend.entity.Phim;
import com.polycinema.backend.dto.LichChieuResponse;
import com.polycinema.backend.service.AuthService;
import com.polycinema.backend.service.LichChieuService;
import com.polycinema.backend.service.PhimService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/phim")
@RequiredArgsConstructor
public class PhimController {

    private final PhimService phimService;
    private final LichChieuService lichChieuService;
    private final AuthService authService;

    @GetMapping("/banner")
    public ResponseEntity<List<Phim>> getBanner() {
        return ResponseEntity.ok(phimService.getBanner());
    }

    @GetMapping("/noi-bat")
    public ResponseEntity<List<Phim>> getNoiBat() {
        List<Phim> all = phimService.findAllNonDeleted();
        List<Phim> result = all.stream()
                .filter(p -> "dang_chieu".equals(p.getTrangThai()))
                .sorted(java.util.Comparator.comparing(
                        p -> p.getDiemDanhGia() != null ? p.getDiemDanhGia() : java.math.BigDecimal.ZERO,
                        java.util.Comparator.reverseOrder()))
                .limit(10)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/dang-chieu")
    public ResponseEntity<List<Phim>> getDangChieu() {
        return ResponseEntity.ok(phimService.getDangChieu());
    }

    @GetMapping("/sap-chieu")
    public ResponseEntity<List<Phim>> getSapChieu() {
        return ResponseEntity.ok(phimService.getSapChieu());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Phim>> timKiem(@RequestParam String q) {
        return ResponseEntity.ok(phimService.timKiem(q));
    }

    @GetMapping
    public ResponseEntity<List<Phim>> getPhim(@RequestParam(required = false) Long theLoaiId) {
        List<Phim> movies = theLoaiId != null
                ? phimService.findByTheLoaiId(theLoaiId)
                : phimService.findAllNonDeleted();
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/{phimId}/lich-chieu")
    public ResponseEntity<List<LichChieuResponse>> getLichChieuByPhim(
            @PathVariable Long phimId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay) {
        LocalDateTime from = ngay != null ? ngay.atStartOfDay() : LocalDateTime.now().minusSeconds(1);
        List<LichChieu> result = lichChieuService.findByPhimAndDate(phimId, from);
        return ResponseEntity.ok(result.stream().map(LichChieuResponse::from).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPhimById(@PathVariable Long id) {
        Phim phim = phimService.getPhimById(id);
        if (phim == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(phim);
    }

    @PostMapping("/{id}/danh-gia")
    public ResponseEntity<?> addRating(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        try {
            Long userId = authService.getUserIdFromToken();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập để đánh giá");
            }
            Integer diem = req.get("diem") != null ? ((Number) req.get("diem")).intValue() : null;
            String binhLuan = (String) req.get("binhLuan");
            String result = phimService.addRating(id, userId, diem, binhLuan);
            if (!result.equals("Đánh giá thành công")) return ResponseEntity.badRequest().body(result);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi đánh giá: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/danh-gia")
    public ResponseEntity<?> getRatings(@PathVariable Long id) {
        try { return ResponseEntity.ok(phimService.getRatingsByPhim(id)); }
        catch (Exception e) { return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi lấy đánh giá: " + e.getMessage()); }
    }
}
