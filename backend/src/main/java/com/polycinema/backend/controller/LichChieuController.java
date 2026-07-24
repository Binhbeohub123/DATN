package com.polycinema.backend.controller;

import com.polycinema.backend.entity.GheNgoi;
import com.polycinema.backend.entity.LichChieu;
import com.polycinema.backend.dto.LichChieuResponse;
import com.polycinema.backend.repository.ChiTietDatGheRepository;
import com.polycinema.backend.repository.GheNgoiRepository;
import com.polycinema.backend.repository.LichChieuRepository;
import com.polycinema.backend.repository.NguoiDungRepository;
import com.polycinema.backend.service.SeatLockService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lich-chieu")
@RequiredArgsConstructor
public class LichChieuController {

    private final LichChieuRepository lichChieuRepository;
    private final GheNgoiRepository gheNgoiRepository;
    private final ChiTietDatGheRepository chiTietDatGheRepository;
    private final SeatLockService seatLockService;
    private final NguoiDungRepository nguoiDungRepository;

    /**
     * GET /api/lich-chieu?phimId=&ngay= — public
     * Lấy lịch chiếu theo phim và ngày (tùy chọn).
     */
    @GetMapping
    public ResponseEntity<List<LichChieu>> getLichChieu(
            @RequestParam(required = false) Long phimId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay) {

        List<LichChieu> result;

        if (phimId != null) {
            LocalDateTime from = ngay != null
                    ? ngay.atStartOfDay()
                    : LocalDateTime.now();
            result = lichChieuRepository
                    .findByPhimIdAndIsDeletedFalseAndThoiGianBatDauAfter(phimId, from.minusSeconds(1));
        } else {
            result = lichChieuRepository.findAll().stream()
                    .filter(lc -> !Boolean.TRUE.equals(lc.getIsDeleted()))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/phim/{phimId}/lich-chieu?ngay= — public (route phụ được frontend gọi)
     * Frontend gọi: /phim/{phimId}/lich-chieu
     * Đây là alias route trên PhimController nhưng ta cũng xử lý ở đây để hỗ trợ cả hai pattern.
     */
    @GetMapping("/phim/{phimId}")
    public ResponseEntity<List<LichChieu>> getLichChieuByPhim(
            @PathVariable Long phimId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay) {

        LocalDateTime from = ngay != null
                ? ngay.atStartOfDay()
                : LocalDateTime.now().minusSeconds(1);

        List<LichChieu> result = lichChieuRepository
                .findByPhimIdAndIsDeletedFalseAndThoiGianBatDauAfter(phimId, from);

        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/lich-chieu/{id}/ghe-trong — public
     * Trả về danh sách ghế của phòng chiếu, kèm trạng thái trống/đã đặt.
     */
    @GetMapping("/{id}/ghe-trong")
    public ResponseEntity<?> getGheTrong(@PathVariable Long id) {
        Optional<LichChieu> opt = lichChieuRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        LichChieu lichChieu = opt.get();
        Long phongChieuId = lichChieu.getPhongChieu().getId();

        // Tất cả ghế trong phòng
        List<GheNgoi> tatCaGhe = gheNgoiRepository.findByPhongChieuId(phongChieuId);

        // ID ghế đã được đặt trong suất chiếu này
        Set<Long> gheDaDat = chiTietDatGheRepository
                .findByLichChieuId(id)
                .stream()
                .map(ct -> ct.getGheNgoi().getId())
                .collect(Collectors.toSet());

        // Map sang DTO kèm trạng thái
        List<GheResponseDTO> response = tatCaGhe.stream()
                .map(ghe -> new GheResponseDTO(
                        ghe.getId(),
                        ghe.getHangGhe(),
                        ghe.getSoGhe(),
                        ghe.getLoaiGhe(),
                        ghe.getHeSoGia(),
                        lichChieu.getGiaCoBan()
                                .multiply(ghe.getHeSoGia()),
                        gheDaDat.contains(ghe.getId()) ? "booked" : "available"
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/lich-chieu/{id}/locked-seats — public
     * Returns IDs of seats locked by OTHER users for a showtime.
     * If the caller is authenticated, their own locks are excluded so their
     * seats render as "selected" (cyan) rather than "locked by other" (amber)
     * when they navigate back to the seat map.
     */
    @GetMapping("/{id}/locked-seats")
    public ResponseEntity<?> getLockedSeats(@PathVariable Long id) {
        // Attempt to resolve the caller's user ID — nullable (public endpoint)
        Long currentUserId = getCurrentUserId();
        return ResponseEntity.ok(seatLockService.getLockedSeatIds(id, currentUserId));
    }

    /**
     * Resolves the authenticated user's DB ID from the JWT principal.
     * Returns null if the request is unauthenticated, the token is expired,
     * or the user cannot be found — never throws.
     */
    private Long getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) return null;
            Object principal = auth.getPrincipal();
            if (!(principal instanceof String)) return null;
            String email = (String) principal;
            if ("anonymousUser".equals(email)) return null;
            return nguoiDungRepository.findByEmail(email).map(u -> u.getId()).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * GET /api/lich-chieu/search?phimId=&thanhPho=&dinhDangId= — public
     * Returns showtimes within the next 30 days as LichChieuResponse DTOs.
     * All query params are optional — omit to skip that filter.
     */
    @GetMapping("/search")
    public ResponseEntity<List<LichChieuResponse>> search(
            @RequestParam(required = false) Long phimId,
            @RequestParam(required = false) String thanhPho,
            @RequestParam(required = false) Long dinhDangId) {

        LocalDateTime from = LocalDateTime.now().minusSeconds(1);
        LocalDateTime to   = from.plusDays(30);

        String city = (thanhPho != null && !thanhPho.isBlank()) ? thanhPho.trim() : null;

        List<LichChieuResponse> result = lichChieuRepository
                .searchPublic(phimId, city, dinhDangId, from, to)
                .stream()
                .map(LichChieuResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ── Inner DTO ──────────────────────────────────────────────
    record GheResponseDTO(
            Long id,
            String hangGhe,
            Integer soGhe,
            String loaiGhe,
            java.math.BigDecimal heSoGia,
            java.math.BigDecimal giaTien,
            String trangThai
    ) {}
}
