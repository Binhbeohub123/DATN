package com.polycinema.backend.controller;

import com.polycinema.backend.entity.LichChieu;
import com.polycinema.backend.entity.Phim;
import com.polycinema.backend.dto.LichChieuResponse;
import com.polycinema.backend.dto.ShowtimeMovieResponse;
import com.polycinema.backend.service.AuthService;
import com.polycinema.backend.service.LichChieuService;
import com.polycinema.backend.service.PhimService;
import com.polycinema.backend.service.SeatLockService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lich-chieu")
@RequiredArgsConstructor
public class LichChieuController {

    private final LichChieuService lichChieuService;
    private final SeatLockService seatLockService;
    private final AuthService authService;
    private final PhimService phimService;

    @GetMapping
    public ResponseEntity<List<LichChieu>> getLichChieu(
            @RequestParam(required = false) Long phimId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay) {
        List<LichChieu> result;
        if (phimId != null) {
            LocalDateTime from = ngay != null ? ngay.atStartOfDay() : LocalDateTime.now();
            result = lichChieuService.findByPhimAndDate(phimId, from.minusSeconds(1));
        } else {
            result = lichChieuService.findAllActive();
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/phim/{phimId}")
    public ResponseEntity<List<LichChieu>> getLichChieuByPhim(
            @PathVariable Long phimId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay) {
        LocalDateTime from = ngay != null ? ngay.atStartOfDay() : LocalDateTime.now().minusSeconds(1);
        return ResponseEntity.ok(lichChieuService.findByPhimAndDate(phimId, from));
    }

    @GetMapping("/phim/{phimId}/available-dates")
    public ResponseEntity<List<LocalDate>> getAvailableDates(@PathVariable Long phimId) {
        return ResponseEntity.ok(lichChieuService.findAvailableDates(phimId));
    }

    @GetMapping("/{id}/ghe-trong")
    public ResponseEntity<?> getGheTrong(@PathVariable Long id) {
        List<Map<String, Object>> result = lichChieuService.getGheTrong(id);
        if (result.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/locked-seats")
    public ResponseEntity<?> getLockedSeats(@PathVariable Long id) {
        Long currentUserId = authService.getUserIdFromToken();
        return ResponseEntity.ok(Map.of(
                "lockedSeatIds",   seatLockService.getLockedSeatIds(id, currentUserId),
                "myLockedSeatIds", seatLockService.getMyActiveLocks(id, currentUserId)
        ));
    }

    @GetMapping("/search")
    public ResponseEntity<List<LichChieuResponse>> search(
            @RequestParam(required = false) Long phimId,
            @RequestParam(required = false) String thanhPho,
            @RequestParam(required = false) Long dinhDangId) {
        String city = (thanhPho != null && !thanhPho.isBlank()) ? thanhPho.trim() : null;
        return ResponseEntity.ok(lichChieuService.searchPublic(phimId, city, dinhDangId));
    }

    @GetMapping("/rap/{rapChieuId}")
    public ResponseEntity<List<ShowtimeMovieResponse>> getLichChieuByRap(
            @PathVariable Long rapChieuId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tu,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate den) {
        LocalDate from = tu  != null ? tu  : LocalDate.now();
        LocalDate to   = den != null ? den : from.plusDays(30);

        List<LichChieu> list = lichChieuService.findByRapAndDateRangeRaw(
                rapChieuId, from.atStartOfDay(), to.plusDays(1).atStartOfDay());

        List<Phim> phims = list.stream()
                .map(LichChieu::getPhim)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        phimService.applyComputedStatus(phims);

        return ResponseEntity.ok(list.stream()
                .map(ShowtimeMovieResponse::from)
                .collect(Collectors.toList()));
    }
}
