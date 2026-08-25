package com.polycinema.backend.service;

import com.polycinema.backend.dto.LichChieuResponse;
import com.polycinema.backend.dto.ShowtimeMovieResponse;
import com.polycinema.backend.entity.GheNgoi;
import com.polycinema.backend.entity.LichChieu;
import com.polycinema.backend.entity.Phim;
import com.polycinema.backend.repository.ChiTietDatGheRepository;
import com.polycinema.backend.repository.GheNgoiRepository;
import com.polycinema.backend.repository.LichChieuRepository;
import com.polycinema.backend.util.SeatDisplayUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LichChieuService {

    private final LichChieuRepository lichChieuRepository;
    private final GheNgoiRepository gheNgoiRepository;
    private final ChiTietDatGheRepository chiTietDatGheRepository;

    public List<LichChieu> findByPhimAndDate(Long phimId, LocalDateTime from) {
        return lichChieuRepository.findByPhimIdAndIsDeletedFalseAndThoiGianBatDauAfter(phimId, from);
    }

    public List<LichChieu> findAllActive() {
        return lichChieuRepository.findAll().stream()
                .filter(lc -> !Boolean.TRUE.equals(lc.getIsDeleted()))
                .collect(Collectors.toList());
    }

    public Optional<LichChieu> findById(Long id) {
        return lichChieuRepository.findById(id);
    }

    public List<Map<String, Object>> getGheTrong(Long lichChieuId) {
        Optional<LichChieu> opt = lichChieuRepository.findById(lichChieuId);
        if (opt.isEmpty()) return Collections.emptyList();

        LichChieu lichChieu = opt.get();
        Long phongChieuId = lichChieu.getPhongChieu().getId();

        List<GheNgoi> tatCaGhe = gheNgoiRepository.findByPhongChieuId(phongChieuId);

        Set<Long> gheDaDat = chiTietDatGheRepository
                .findByLichChieuId(lichChieuId)
                .stream()
                .map(ct -> ct.getGheNgoi().getId())
                .collect(Collectors.toSet());

        // Nhãn hiển thị tính từ TOÀN BỘ ghế phòng (có cả ô 'trống') để đếm đúng
        Map<Long, Integer> nhanHienThi = SeatDisplayUtil.buildRoomLabels(tatCaGhe);
        SeatDisplayUtil.applyLabels(tatCaGhe, nhanHienThi);
        SeatDisplayUtil.warnMissing(tatCaGhe, "GET /lich-chieu/{id}/ghe-trong (va POS /staff/pos/ghe)");

        return tatCaGhe.stream()
                // 'trống' cells are aisles/gaps in the room layout — customers
                // (and POS) must never see or book them, so filter them out.
                .filter(ghe -> !"trống".equals(ghe.getLoaiGhe()))
                .map(ghe -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", ghe.getId());
                    m.put("hangGhe", ghe.getHangGhe());
                    m.put("soGhe", ghe.getSoGhe());
                    m.put("soGheHienThi", ghe.getSoGheHienThi());
                    m.put("loaiGhe", ghe.getLoaiGhe());
                    m.put("heSoGia", ghe.getHeSoGia());
                    m.put("giaTien", lichChieu.getGiaCoBan().multiply(ghe.getHeSoGia()));
                    m.put("trangThai", gheDaDat.contains(ghe.getId()) ? "booked" : "available");
                    return m;
                })
                .collect(Collectors.toList());
    }

    public List<LichChieuResponse> searchPublic(Long phimId, String thanhPho, Long dinhDangId) {
        LocalDateTime from = LocalDateTime.now().minusSeconds(1);
        LocalDateTime to = from.plusDays(30);
        return lichChieuRepository.searchPublic(phimId, thanhPho, dinhDangId, from, to)
                .stream()
                .map(LichChieuResponse::from)
                .collect(Collectors.toList());
    }

    public List<ShowtimeMovieResponse> findByRapAndDateRange(Long rapChieuId, LocalDateTime from, LocalDateTime to) {
        return lichChieuRepository.findByRapChieuIdAndRange(rapChieuId, from, to)
                .stream()
                .map(ShowtimeMovieResponse::from)
                .collect(Collectors.toList());
    }

    public List<LichChieu> findByRapAndDateRangeRaw(Long rapChieuId, LocalDateTime from, LocalDateTime to) {
        return lichChieuRepository.findByRapChieuIdAndRange(rapChieuId, from, to);
    }

    public List<LichChieu> findTodayByRapChieuId(Long rapChieuId, LocalDateTime from, LocalDateTime to) {
        return lichChieuRepository.findTodayByRapChieuId(rapChieuId, from, to);
    }

    // ── Admin methods ─────────────────────────────────────────

    public List<LichChieu> findAll() {
        return lichChieuRepository.findAll();
    }

    public LichChieu save(LichChieu lichChieu) {
        return lichChieuRepository.save(lichChieu);
    }

    public org.springframework.data.domain.Page<LichChieu> findAdminPage(
            LocalDateTime dateFrom, LocalDateTime dateTo, Long rapChieuId, Long phongChieuId,
            org.springframework.data.domain.Pageable pageable) {
        return lichChieuRepository.findAdminPage(dateFrom, dateTo, rapChieuId, phongChieuId, pageable);
    }

    public List<com.polycinema.backend.entity.ChiTietDatGhe> findChiTietDatGheByLichChieuId(Long lichChieuId) {
        return chiTietDatGheRepository.findByLichChieuId(lichChieuId);
    }
}
