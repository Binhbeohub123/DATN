package com.polycinema.backend.service;

import com.polycinema.backend.entity.KhuyenMai;
import com.polycinema.backend.entity.Phim;
import com.polycinema.backend.repository.DatVeRepository;
import com.polycinema.backend.repository.KhuyenMaiRepository;
import com.polycinema.backend.repository.NguoiDungRepository;
import com.polycinema.backend.repository.PhimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KhuyenMaiService {

    private final KhuyenMaiRepository khuyenMaiRepository;
    private final PhimRepository phimRepository;
    private final DatVeRepository datVeRepository;
    private final NguoiDungRepository nguoiDungRepository;

    public List<KhuyenMai> getAllActive() {
        return khuyenMaiRepository.findByDangHoatDongTrue();
    }

    public List<KhuyenMai> getAllWithPhims() {
        List<KhuyenMai> list = khuyenMaiRepository.findAllWithPhims();
        for (KhuyenMai km : list) {
            long used = datVeRepository.countByKhuyenMaiIdAndTrangThaiNot(km.getId(), "cancelled");
            km.setDaSuDung((int) used);
        }
        return list;
    }

    public List<KhuyenMai> getActiveWithPhims() {
        return khuyenMaiRepository.findActiveWithPhims(LocalDate.now());
    }

    public Map<String, Object> validate(String maKhuyenMai, BigDecimal tongTien, Long userId) {
        if (maKhuyenMai == null || maKhuyenMai.isBlank()) {
            return Map.of("valid", false, "discountAmount", BigDecimal.ZERO,
                    "message", "Mã khuyến mãi không được để trống");
        }

        KhuyenMai km = khuyenMaiRepository.findByMaKhuyenMai(maKhuyenMai.trim().toUpperCase())
                .orElse(null);
        if (km == null) {
            return Map.of("valid", false, "discountAmount", BigDecimal.ZERO,
                    "message", "Mã khuyến mãi không tồn tại");
        }
        if (!Boolean.TRUE.equals(km.getDangHoatDong())) {
            return Map.of("valid", false, "discountAmount", BigDecimal.ZERO,
                    "message", "Mã khuyến mãi đã hết hiệu lực");
        }

        LocalDate today = LocalDate.now();
        if (km.getNgayBatDau() != null && today.isBefore(km.getNgayBatDau())) {
            return Map.of("valid", false, "discountAmount", BigDecimal.ZERO,
                    "message", "Mã khuyến mãi chưa có hiệu lực");
        }
        if (km.getNgayKetThuc() != null && today.isAfter(km.getNgayKetThuc())) {
            return Map.of("valid", false, "discountAmount", BigDecimal.ZERO,
                    "message", "Mã khuyến mãi đã hết hạn");
        }

        long usedTotal = datVeRepository.countByKhuyenMaiIdAndTrangThaiNot(km.getId(), "cancelled");
        if (km.getGioiHanSuDung() != null && usedTotal >= km.getGioiHanSuDung()) {
            return Map.of("valid", false, "discountAmount", BigDecimal.ZERO,
                    "message", "Mã khuyến mãi đã hết lượt sử dụng");
        }

        if (userId != null) {
            long userUsed = datVeRepository.countByKhuyenMaiIdAndNguoiDungIdAndTrangThaiNot(
                    km.getId(), userId, "cancelled");
            if (userUsed > 0) {
                return Map.of("valid", false, "discountAmount", BigDecimal.ZERO,
                        "message", "Bạn đã sử dụng mã khuyến mãi này rồi");
            }
        }

        if (km.getDonHangToiThieu() != null
                && tongTien.compareTo(km.getDonHangToiThieu()) < 0) {
            return Map.of("valid", false, "discountAmount", BigDecimal.ZERO,
                    "message", "Đơn hàng tối thiểu " + km.getDonHangToiThieu().toPlainString() + " VND");
        }

        BigDecimal discountAmount;
        if ("percent".equalsIgnoreCase(km.getLoaiGiamGia())) {
            discountAmount = tongTien.multiply(km.getGiaTriGiam())
                    .divide(BigDecimal.valueOf(100));
            if (km.getGiaTriGiamToiDa() != null) {
                discountAmount = discountAmount.min(km.getGiaTriGiamToiDa());
            }
        } else {
            discountAmount = km.getGiaTriGiam().min(tongTien);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("valid", true);
        result.put("discountAmount", discountAmount);
        result.put("message", "Áp dụng thành công: " + km.getTenKhuyenMai());
        result.put("tenKhuyenMai", km.getTenKhuyenMai());
        result.put("loaiGiamGia", km.getLoaiGiamGia());
        result.put("giaTriGiam", km.getGiaTriGiam());
        result.put("giaTriGiamToiDa", km.getGiaTriGiamToiDa());
        result.put("donHangToiThieu", km.getDonHangToiThieu());
        result.put("id", km.getId());
        result.put("maKhuyenMai", km.getMaKhuyenMai());
        return result;
    }

    public KhuyenMai create(Map<String, Object> body) {
        KhuyenMai km = new KhuyenMai();
        if (body.get("maKhuyenMai") != null)
            km.setMaKhuyenMai(((String) body.get("maKhuyenMai")).trim().toUpperCase());
        applyFields(km, body);
        if (km.getDaSuDung() == null) km.setDaSuDung(0);
        if (km.getDangHoatDong() == null) km.setDangHoatDong(true);
        return khuyenMaiRepository.save(km);
    }

    public KhuyenMai update(Long id, Map<String, Object> body) {
        KhuyenMai km = khuyenMaiRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khuyến mãi"));
        applyFields(km, body);
        return khuyenMaiRepository.save(km);
    }

    public void deactivate(Long id) {
        KhuyenMai km = khuyenMaiRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khuyến mãi"));
        km.setDangHoatDong(false);
        khuyenMaiRepository.save(km);
    }

    @SuppressWarnings("unchecked")
    private void applyFields(KhuyenMai km, Map<String, Object> body) {
        if (body.containsKey("tenKhuyenMai")    && body.get("tenKhuyenMai")    != null) km.setTenKhuyenMai((String) body.get("tenKhuyenMai"));
        if (body.containsKey("moTa")            && body.get("moTa")            != null) km.setMoTa((String) body.get("moTa"));
        if (body.containsKey("loaiGiamGia")     && body.get("loaiGiamGia")     != null) km.setLoaiGiamGia((String) body.get("loaiGiamGia"));
        if (body.containsKey("giaTriGiam")      && body.get("giaTriGiam")      != null) km.setGiaTriGiam(new BigDecimal(body.get("giaTriGiam").toString()));
        if (body.containsKey("giaTriGiamToiDa") && body.get("giaTriGiamToiDa") != null) km.setGiaTriGiamToiDa(new BigDecimal(body.get("giaTriGiamToiDa").toString()));
        if (body.containsKey("donHangToiThieu") && body.get("donHangToiThieu") != null) km.setDonHangToiThieu(new BigDecimal(body.get("donHangToiThieu").toString()));
        if (body.containsKey("gioiHanSuDung")   && body.get("gioiHanSuDung")   != null) km.setGioiHanSuDung(((Number) body.get("gioiHanSuDung")).intValue());
        if (body.containsKey("ngayBatDau")      && body.get("ngayBatDau")      != null) km.setNgayBatDau(LocalDate.parse((String) body.get("ngayBatDau")));
        if (body.containsKey("ngayKetThuc")     && body.get("ngayKetThuc")     != null) km.setNgayKetThuc(LocalDate.parse((String) body.get("ngayKetThuc")));
        if (body.containsKey("dangHoatDong")    && body.get("dangHoatDong")    != null) km.setDangHoatDong((Boolean) body.get("dangHoatDong"));

        if (body.containsKey("phimIds")) {
            List<?> rawIds = (List<?>) body.get("phimIds");
            List<Phim> phims;
            if (rawIds == null || rawIds.isEmpty()) {
                phims = new ArrayList<>();
            } else {
                phims = rawIds.stream()
                        .map(raw -> {
                            Long pid = ((Number) raw).longValue();
                            return phimRepository.findById(pid)
                                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phim id=" + pid));
                        })
                        .collect(Collectors.toList());
            }
            km.getPhims().clear();
            km.getPhims().addAll(phims);
        }
    }

    public Long getUserIdByEmail(String email) {
        return nguoiDungRepository.findByEmail(email).map(u -> u.getId()).orElse(null);
    }
}
