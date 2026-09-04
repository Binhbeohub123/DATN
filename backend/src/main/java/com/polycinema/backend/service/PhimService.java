package com.polycinema.backend.service;

import com.polycinema.backend.entity.Phim;
import com.polycinema.backend.entity.LichChieu;
import com.polycinema.backend.entity.DanhGiaPhim;
import com.polycinema.backend.entity.NguoiDung;
import com.polycinema.backend.repository.BannerRepository;
import com.polycinema.backend.repository.LichChieuRepository;
import com.polycinema.backend.repository.PhimRepository;
import com.polycinema.backend.repository.DanhGiaPhimRepository;
import com.polycinema.backend.repository.NguoiDungRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.polycinema.backend.entity.Banner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PhimService {

    private final PhimRepository phimRepository;
    private final LichChieuRepository lichChieuRepository;
    private final DanhGiaPhimRepository danhGiaPhimRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final BannerRepository bannerRepository;

    // ── Computed status rules (nextSession-based, 30-day threshold) ─────────
    // no showtimes at all (or only past)  → da_ket_thuc
    // nextSession is >= 30 days away      → sap_chieu
    // nextSession is <  30 days away      → dang_chieu

    /**
     * Compute status from the next upcoming showtime.
     * @param nextSession the earliest future showtime (thoiGianBatDau >= now), or null
     * @return one of: "da_ket_thuc", "sap_chieu", "dang_chieu"
     */
    public static String computeStatus(LocalDateTime nextSession) {
        if (nextSession == null) return "da_ket_thuc";
        LocalDate today = LocalDate.now();
        LocalDate nextDate = nextSession.toLocalDate();
        if (nextDate.isBefore(today.plusDays(30))) return "dang_chieu";
        return "sap_chieu";
    }

    /**
     * Apply computed trangThai to every movie in the list using ONE grouped query.
     * Uses the earliest future showtime per movie (nextSession rule).
     * Modifies each Phim object in-place (does NOT persist — read-time decoration only).
     */
    public void applyComputedStatus(List<Phim> movies) {
        if (movies == null || movies.isEmpty()) return;
        List<Object[]> rows = lichChieuRepository.findEarliestSessionByPhim();
        // Build a map: phimId → firstSession (earliest showtime, possibly in the past)
        Map<Long, LocalDateTime> earliest = new HashMap<>();
        for (Object[] row : rows) {
            Long phimId = ((Number) row[0]).longValue();
            LocalDateTime firstSession = (LocalDateTime) row[1];
            earliest.put(phimId, firstSession);
        }
        LocalDateTime now = LocalDateTime.now();
        for (Phim p : movies) {
            Long id = p.getId();
            LocalDateTime firstSession = earliest.get(id);
            // If the earliest showtime is in the past, we need the next future one
            if (firstSession != null && firstSession.isBefore(now)) {
                // Batch approach: for past-only movies, fetch next future session individually
                List<LichChieu> next = lichChieuRepository.findNextSessionByPhimId(id, now);
                LocalDateTime nextSession = next.isEmpty() ? null : next.get(0).getThoiGianBatDau();
                p.setTrangThai(computeStatus(nextSession));
            } else {
                p.setTrangThai(computeStatus(firstSession));
            }
        }
    }

    /** Single-movie variant */
    public void applyComputedStatus(Phim phim) {
        if (phim == null) return;
        LocalDateTime now = LocalDateTime.now();
        List<LichChieu> next = lichChieuRepository.findNextSessionByPhimId(phim.getId(), now);
        LocalDateTime nextSession = next.isEmpty() ? null : next.get(0).getThoiGianBatDau();
        phim.setTrangThai(computeStatus(nextSession));
    }

    public List<Phim> getDangChieu() {
        List<Phim> all = phimRepository.findByIsDeletedFalse();
        applyComputedStatus(all);
        return all.stream().filter(p -> "dang_chieu".equals(p.getTrangThai())).collect(Collectors.toList());
    }

    public List<Phim> getSapChieu() {
        List<Phim> all = phimRepository.findByIsDeletedFalse();
        applyComputedStatus(all);
        return all.stream().filter(p -> "sap_chieu".equals(p.getTrangThai())).collect(Collectors.toList());
    }

    public List<Phim> getBanner() {
        List<Phim> dang = getDangChieu();
        return dang.size() > 5 ? dang.subList(0, 5) : dang;
    }

    public List<Banner> getActiveBanners() {
        return bannerRepository.findActiveBannersForDate(LocalDate.now());
    }

    public List<Phim> timKiem(String tenPhim) {
        List<Phim> list = phimRepository.findByTenPhimContainingIgnoreCaseAndIsDeletedFalse(tenPhim);
        applyComputedStatus(list);
        return list;
    }

    public List<Phim> findByTheLoaiId(Long theLoaiId) {
        List<Phim> list = phimRepository.findByTheLoaiId(theLoaiId);
        applyComputedStatus(list);
        return list;
    }

    public List<Phim> findAllNonDeleted() {
        List<Phim> list = phimRepository.findByIsDeletedFalse();
        applyComputedStatus(list);
        return list;
    }

    public List<LichChieu> findLichChieuByPhim(Long phimId, LocalDateTime from) {
        return lichChieuRepository.findByPhimIdAndIsDeletedFalseAndThoiGianBatDauAfter(phimId, from);
    }

    public Phim getPhimById(Long id) {
        Phim phim = phimRepository.findById(id).orElse(null);
        applyComputedStatus(phim);
        return phim;
    }

    // ================= ADD/UPDATE RATING =================
    @Transactional
    public String addRating(Long phimId, Long nguoiDungId, Integer diem, String binhLuan) {
        // Validate rating score
        if (diem == null || diem < 1 || diem > 10) {
            return "Điểm đánh giá phải từ 1 đến 10";
        }

        // Check if movie exists
        Phim phim = phimRepository.findById(phimId).orElse(null);
        if (phim == null) {
            return "Không tìm thấy phim";
        }

        // Check if user exists
        NguoiDung nguoiDung = nguoiDungRepository.findById(nguoiDungId).orElse(null);
        if (nguoiDung == null) {
            return "Không tìm thấy người dùng";
        }

        // Check if user already rated this movie
        DanhGiaPhim existing = danhGiaPhimRepository
                .findByPhimIdAndNguoiDungId(phimId, nguoiDungId)
                .orElse(null);

        if (existing != null) {
            // Update existing rating
            existing.setDiem(diem);
            existing.setBinhLuan(binhLuan != null ? binhLuan.trim() : null);
            danhGiaPhimRepository.save(existing);
        } else {
            // Create new rating
            DanhGiaPhim danhGia = new DanhGiaPhim();
            danhGia.setPhim(phim);
            danhGia.setNguoiDung(nguoiDung);
            danhGia.setDiem(diem);
            danhGia.setBinhLuan(binhLuan != null ? binhLuan.trim() : null);
            danhGiaPhimRepository.save(danhGia);
        }

        // Recalculate movie's average rating
        updateMovieRating(phimId);

        return "Đánh giá thành công";
    }

    // ================= UPDATE MOVIE AVERAGE RATING =================
    private void updateMovieRating(Long phimId) {
        List<DanhGiaPhim> ratings = danhGiaPhimRepository.findByPhimId(phimId);

        if (ratings.isEmpty()) {
            return;
        }

        double average = ratings.stream()
                .mapToInt(DanhGiaPhim::getDiem)
                .average()
                .orElse(0.0);

        Phim phim = phimRepository.findById(phimId).orElse(null);
        if (phim != null) {
            phim.setDiemDanhGia(java.math.BigDecimal.valueOf(average));
            phim.setSoLuongDanhGia(ratings.size());
            phimRepository.save(phim);
        }
    }

    // ================= GET RATINGS FOR MOVIE =================
    public List<DanhGiaPhim> getRatingsByPhim(Long phimId) {
        return danhGiaPhimRepository.findByPhimId(phimId);
    }

    // ================= ADMIN METHODS =================

    public List<Phim> findAllIncludingDeleted() {
        return phimRepository.findAll();
    }

    public List<Phim> findAllActive() {
        return phimRepository.findByIsDeletedFalse();
    }

    public Phim save(Phim phim) {
        return phimRepository.save(phim);
    }

    public Optional<Phim> findByIdOptional(Long id) {
        return phimRepository.findById(id);
    }
}
