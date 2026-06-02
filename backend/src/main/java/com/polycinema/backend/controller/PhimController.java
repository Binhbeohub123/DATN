package com.polycinema.backend.controller;

import com.polycinema.backend.entity.Phim;
import com.polycinema.backend.entity.LichChieu;
import com.polycinema.backend.dto.LichChieuResponse;
import com.polycinema.backend.repository.NguoiDungRepository;
import com.polycinema.backend.service.PhimService;
import com.polycinema.backend.repository.LichChieuRepository;
import lombok.RequiredArgsConstructor;
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
@CrossOrigin(origins = "http://localhost:5173")
public class PhimController {

    private final PhimService phimService;
    private final LichChieuRepository lichChieuRepository;
    private final NguoiDungRepository nguoiDungRepository;

    // GET /api/phim/banner
    @GetMapping("/banner")
    public ResponseEntity<List<Phim>> getBanner() {
        List<Phim> banners = phimService.getBanner();
        return ResponseEntity.ok(banners);
    }

    // GET /api/phim/dang-chieu
    @GetMapping("/dang-chieu")
    public ResponseEntity<List<Phim>> getDangChieu() {
        List<Phim> phimDangChieu = phimService.getDangChieu();
        return ResponseEntity.ok(phimDangChieu);
    }

    // GET /api/phim/sap-chieu
    @GetMapping("/sap-chieu")
    public ResponseEntity<List<Phim>> getSapChieu() {
        List<Phim> phimSapChieu = phimService.getSapChieu();
        return ResponseEntity.ok(phimSapChieu);
    }

    // GET /api/phim/search?q=tên phim
    @GetMapping("/search")
    public ResponseEntity<List<Phim>> timKiem(@RequestParam String q) {
        List<Phim> ketQua = phimService.timKiem(q);
        return ResponseEntity.ok(ketQua);
    }

    // GET /api/phim/{phimId}/lich-chieu — Lấy lịch chiếu của phim
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

    // GET /api/phim/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getPhimById(@PathVariable Long id) {
        Phim phim = phimService.getPhimById(id);
        if (phim == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(phim);
    }

    // POST /api/phim/{id}/danh-gia — Add/Update movie rating
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
                    ? ((Number) req.get("diem")).intValue()
                    : null;
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

    // GET /api/phim/{id}/danh-gia — Get all ratings for a movie
    @GetMapping("/{id}/danh-gia")
    public ResponseEntity<?> getRatings(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(phimService.getRatingsByPhim(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi lấy đánh giá: " + e.getMessage());
        }
    }

    // Helper: Get userId from JWT token (principal is email)
    private Long getUserIdFromToken() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                Object principal = auth.getPrincipal();
                String email = principal instanceof String ? (String) principal : null;
                if (email != null && !email.equals("anonymousUser")) {
                    return nguoiDungRepository.findByEmail(email)
                            .map(u -> u.getId())
                            .orElse(null);
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
