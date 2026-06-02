package com.polycinema.backend.controller;

import com.polycinema.backend.entity.GheNgoi;
import com.polycinema.backend.entity.LichChieu;
import com.polycinema.backend.repository.ChiTietDatGheRepository;
import com.polycinema.backend.repository.GheNgoiRepository;
import com.polycinema.backend.repository.LichChieuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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
@CrossOrigin(origins = "http://localhost:5173")
public class LichChieuController {

    private final LichChieuRepository lichChieuRepository;
    private final GheNgoiRepository gheNgoiRepository;
    private final ChiTietDatGheRepository chiTietDatGheRepository;

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
