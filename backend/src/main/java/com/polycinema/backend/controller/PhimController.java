package com.polycinema.backend.controller;

import com.polycinema.backend.entity.LichChieu;
import com.polycinema.backend.entity.Phim;
import com.polycinema.backend.dto.LichChieuResponse;
import com.polycinema.backend.repository.LichChieuRepository;
import com.polycinema.backend.repository.NguoiDungRepository;
import com.polycinema.backend.repository.PhimRepository;
import com.polycinema.backend.service.PhimService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final LichChieuRepository lichChieuRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final PhimRepository phimRepository;

    // GET /api/phim/banner
    @GetMapping("/banner")
    public ResponseEntity<List<Phim>> getBanner() {
        return ResponseEntity.ok(phimService.getBanner());
    }

    /**
     * GET /api/phim/noi-bat
     * Top 10 now-showing movies ordered by rating (diemDanhGia) DESC.
     */
    @GetMapping("/noi-bat")
    public ResponseEntity<List<Phim>> getNoiBat() {
        List<Phim> all = phimRepository.findByIsDeletedFalse();
        phimService.applyComputedStatus(all);
        List<Phim> result = all.stream()
                .filter(p -> "dang_chieu".equals(p.getTrangThai()))
                .sorted(java.util.Comparator.comparing(
                        p -> p.getDiemDanhGia() != null ? p.getDiemDanhGia() : java.math.BigDecimal.ZERO,
                        java.util.Comparator.reverseOrder()))
                .limit(10)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // GET /api/phim/dang-chieu
    @GetMapping("/dang-chieu")
    public ResponseEntity<List<Phim>> getDangChieu() {
        return ResponseEntity.ok(phimService.getDangChieu());
    }

    // GET /api/phim/sap-chieu
    @GetMapping("/sap-chieu")
    public ResponseEntity<List<Phim>> getSapChieu() {
        return ResponseEntity.ok(phimService.getSapChieu());
    }

    // GET /api/phim/search?q=tên phim
    @GetMapping("/search")
    public ResponseEntity<List<Phim>> timKiem(@RequestParam String q) {
        return ResponseEntity.ok(phimService.timKiem(q));
    }

    /**
     * GET /api/phim?theLoaiId=X — filter by genre (optional).
     * Without theLoaiId returns all non-deleted movies.
     */
    @GetMapping
    public ResponseEntity<List<Phim>> getPhim(
            @RequestParam(required = false) Long theLoaiId) {
        List<Phim> movies = theLoaiId != null
                ? phimRepository.findByTheLoaiId(theLoaiId)
                : phimRepository.findByIsDeletedFalse();
        phimService.applyComputedStatus(movies);
        return ResponseEntity.ok(movies);
    }

    // GET /api/phim/{phimId}/lich-chieu
    @GetMapping("/{phimId}/lich-chieu")
    public ResponseEntity<List<LichChieuResponse>> getLichChieuByPhim(
            @PathVariable Long phimId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay) {

        LocalDateTime from = ngay != null
                ? ngay.atStartOfDay()
                : LocalDateTime.now().minusSeconds(1);

        List<LichChieu> result = lichChieuRepository
                .findByPhimIdAndIsDeletedFalseAndThoiGianBatDauAfter(phimId, from);

        List<LichChieuResponse> response = result.stream()
                .map(LichChieuResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // GET /api/phim/{id} — trangThai is computed from showtimes by phimService.getPhimById
    @GetMapping("/{id}")
    public ResponseEntity<?> getPhimById(@PathVariable Long id) {
        Phim phim = phimService.getPhimById(id);  // applies computed status
        if (phim == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(phim);
    }

    // POST /api/phim/{id}/danh-gia
    @PostMapping("/{id}/danh-gia")
    public ResponseEntity<?> addRating(
            @PathVariable Long id,
            @RequestBody Map<String, Object> req) {
        try {
            Long userId = getUserIdFromToken();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Vui lòng đăng nhập để đánh giá");
            }
            Integer diem = req.get("diem") != null
                    ? ((Number) req.get("diem")).intValue() : null;
            String binhLuan = (String) req.get("binhLuan");
            String result = phimService.addRating(id, userId, diem, binhLuan);
            if (!result.equals("Đánh giá thành công")) {
                return ResponseEntity.badRequest().body(result);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi đánh giá: " + e.getMessage());
        }
    }

    // GET /api/phim/{id}/danh-gia
    @GetMapping("/{id}/danh-gia")
    public ResponseEntity<?> getRatings(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(phimService.getRatingsByPhim(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi lấy đánh giá: " + e.getMessage());
        }
    }

    private Long getUserIdFromToken() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                Object principal = auth.getPrincipal();
                String email = principal instanceof String ? (String) principal : null;
                if (email != null && !email.equals("anonymousUser")) {
                    return nguoiDungRepository.findByEmail(email)
                            .map(u -> u.getId()).orElse(null);
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
