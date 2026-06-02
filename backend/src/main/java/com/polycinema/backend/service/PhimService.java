package com.polycinema.backend.service;

import com.polycinema.backend.entity.Phim;
import com.polycinema.backend.entity.DanhGiaPhim;
import com.polycinema.backend.entity.NguoiDung;
import com.polycinema.backend.repository.PhimRepository;
import com.polycinema.backend.repository.DanhGiaPhimRepository;
import com.polycinema.backend.repository.NguoiDungRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PhimService {

    private final PhimRepository phimRepository;
    private final DanhGiaPhimRepository danhGiaPhimRepository;
    private final NguoiDungRepository nguoiDungRepository;

    public List<Phim> getDangChieu() {
        return phimRepository.findByTrangThaiAndIsDeletedFalse("dang_chieu");
    }

    public List<Phim> getSapChieu() {
        return phimRepository.findByTrangThaiAndIsDeletedFalse("sap_chieu");
    }

    public List<Phim> getBanner() {
        // Banner hiển thị phim đang chiếu (tối đa 5 phim)
        List<Phim> dangChieu = phimRepository.findByTrangThaiAndIsDeletedFalse("dang_chieu");
        if (dangChieu.size() > 5) {
            return dangChieu.subList(0, 5);
        }
        return dangChieu;
    }

    public List<Phim> timKiem(String tenPhim) {
        return phimRepository.findByTenPhimContainingIgnoreCaseAndIsDeletedFalse(tenPhim);
    }

    public Phim getPhimById(Long id) {
        return phimRepository.findById(id).orElse(null);
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
}
