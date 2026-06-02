package com.polycinema.backend.service;

import com.polycinema.backend.entity.*;
import com.polycinema.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DatVeService {

    private final DatVeRepository datVeRepository;
    private final ChiTietDatGheRepository chiTietDatGheRepository;
    private final ChiTietDatSanPhamRepository chiTietDatSanPhamRepository;
    private final LichChieuRepository lichChieuRepository;
    private final GheNgoiRepository gheNgoiRepository;
    private final SanPhamRepository sanPhamRepository;
    private final KhuyenMaiRepository khuyenMaiRepository;
    private final NguoiDungRepository nguoiDungRepository;

    /**
     * Tạo đơn đặt vé
     * @param userId ID của người dùng
     * @param lichChieuId ID của lịch chiếu
     * @param gheIds Danh sách ID ghế
     * @param comboData Danh sách combo: [{id: sanPhamId, soLuong: quantity}, ...]
     * @param maKhuyenMai Mã khuyến mãi (optional)
     * @param diemSuDung Điểm sử dụng (optional)
     * @return DatVe object
     */
    @Transactional
    public DatVe createBooking(
            Long userId,
            Long lichChieuId,
            List<Long> gheIds,
            List<Map<String, Object>> comboData,
            String maKhuyenMai,
            Integer diemSuDung
    ) {
        // 1. Validate cơ bản
        if (gheIds == null || gheIds.isEmpty()) {
            throw new IllegalArgumentException("Phải chọn ít nhất 1 ghế");
        }

        if (gheIds.size() > 8) {
            throw new IllegalArgumentException("Tối đa 8 ghế/lần đặt");
        }

        // 2. Lấy lịch chiếu
        LichChieu lichChieu = lichChieuRepository.findById(lichChieuId)
                .orElseThrow(() -> new IllegalArgumentException("Lịch chiếu không tồn tại"));

        // 3. Lấy người dùng
        NguoiDung nguoiDung = nguoiDungRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        // 4. Validate ghế - kiểm tra tồn tại và còn trống
        List<GheNgoi> gheList = new ArrayList<>();
        for (Long gheId : gheIds) {
            GheNgoi ghe = gheNgoiRepository.findById(gheId)
                    .orElseThrow(() -> new IllegalArgumentException("Ghế ID " + gheId + " không tồn tại"));

            // Kiểm tra ghế thuộc phòng chiếu của lịch chiếu này
            if (!ghe.getPhongChieu().getId().equals(lichChieu.getPhongChieu().getId())) {
                throw new IllegalArgumentException("Ghế ID " + gheId + " không thuộc phòng chiếu này");
            }

            gheList.add(ghe);
        }

        // 5. Kiểm tra ghế đã được đặt chưa
        Set<Long> gheDaDat = chiTietDatGheRepository.findByLichChieuId(lichChieuId)
                .stream()
                .map(ct -> ct.getGheNgoi().getId())
                .collect(Collectors.toSet());

        for (Long gheId : gheIds) {
            if (gheDaDat.contains(gheId)) {
                throw new IllegalArgumentException("Ghế ID " + gheId + " đã được đặt");
            }
        }

        // 6. Tính tổng tiền ghế
        BigDecimal tongTienGhe = BigDecimal.ZERO;
        for (GheNgoi ghe : gheList) {
            BigDecimal giaGhe = lichChieu.getGiaCoBan().multiply(ghe.getHeSoGia());
            tongTienGhe = tongTienGhe.add(giaGhe);
        }

        // 7. Tính tổng tiền combo
        BigDecimal tongTienCombo = BigDecimal.ZERO;
        List<ChiTietDatSanPham> chiTietComboList = new ArrayList<>();

        if (comboData != null && !comboData.isEmpty()) {
            for (Map<String, Object> combo : comboData) {
                Long sanPhamId = ((Number) combo.get("id")).longValue();
                Integer soLuong = ((Number) combo.get("soLuong")).intValue();

                SanPham sanPham = sanPhamRepository.findById(sanPhamId)
                        .orElseThrow(() -> new IllegalArgumentException("Sản phẩm ID " + sanPhamId + " không tồn tại"));

                BigDecimal giaSanPham = sanPham.getGia();
                BigDecimal tienCombo = giaSanPham.multiply(BigDecimal.valueOf(soLuong));
                tongTienCombo = tongTienCombo.add(tienCombo);

                // Chuẩn bị chi tiết combo
                ChiTietDatSanPham chiTiet = new ChiTietDatSanPham();
                chiTiet.setSanPham(sanPham);
                chiTiet.setSoLuong(soLuong);
                chiTiet.setGiaLucMua(giaSanPham);
                chiTietComboList.add(chiTiet);
            }
        }

        // 8. Tính tổng tiền gốc (ghế + combo)
        BigDecimal tongTienGoc = tongTienGhe.add(tongTienCombo);

        // 9. Áp dụng khuyến mãi
        KhuyenMai khuyenMai = null;
        BigDecimal tienGiamKhuyenMai = BigDecimal.ZERO;

        if (maKhuyenMai != null && !maKhuyenMai.isEmpty()) {
            khuyenMai = khuyenMaiRepository.findByMaKhuyenMai(maKhuyenMai)
                    .orElseThrow(() -> new IllegalArgumentException("Mã khuyến mãi không hợp lệ"));

            // Tính tiền giảm
            if ("percent".equalsIgnoreCase(khuyenMai.getLoaiGiamGia())) {
                tienGiamKhuyenMai = tongTienGoc
                        .multiply(khuyenMai.getGiaTriGiam())
                        .divide(BigDecimal.valueOf(100));
                // Giới hạn tiền giảm tối đa
                if (khuyenMai.getGiaTriGiamToiDa() != null) {
                    tienGiamKhuyenMai = tienGiamKhuyenMai.min(khuyenMai.getGiaTriGiamToiDa());
                }
            } else {
                // Giảm cố định
                tienGiamKhuyenMai = khuyenMai.getGiaTriGiam().min(tongTienGoc);
            }
        }

        // 10. Áp dụng điểm thích lập
        BigDecimal tienGiamTuDiem = BigDecimal.ZERO;
        if (diemSuDung != null && diemSuDung > 0) {
            // 1 điểm = 1000 VND
            tienGiamTuDiem = BigDecimal.valueOf(diemSuDung * 1000L);
            // Không được giảm hơn tổng tiền
            tienGiamTuDiem = tienGiamTuDiem.min(tongTienGoc);
        }

        // 11. Tính tổng tiền thanh toán
        BigDecimal tongTienThanhToan = tongTienGoc
                .subtract(tienGiamKhuyenMai)
                .subtract(tienGiamTuDiem);
        if (tongTienThanhToan.compareTo(BigDecimal.ZERO) < 0) {
            tongTienThanhToan = BigDecimal.ZERO;
        }

        // 12. Tạo đơn đặt vé
        DatVe datVe = new DatVe();
        datVe.setMaDatVe(generateBookingCode());
        datVe.setNguoiDung(nguoiDung);
        datVe.setLichChieu(lichChieu);
        datVe.setKhuyenMai(khuyenMai);
        datVe.setTongTienGoc(tongTienGoc);
        datVe.setTienGiamKhuyenMai(tienGiamKhuyenMai);
        datVe.setDiemSuDung(diemSuDung != null ? diemSuDung : 0);
        datVe.setTienGiamTuDiem(tienGiamTuDiem);
        datVe.setTongTienThanhToan(tongTienThanhToan);
        datVe.setTrangThai("pending");
        datVe.setTrangThaiThanhToan("unpaid");
        datVe.setHetHanGiuGhe(LocalDateTime.now().plusMinutes(15)); // Giữ ghế 15 phút

        DatVe savedDatVe = datVeRepository.save(datVe);

        // 13. Lưu chi tiết đặt ghế
        for (GheNgoi ghe : gheList) {
            ChiTietDatGhe chiTiet = new ChiTietDatGhe();
            chiTiet.setDatVe(savedDatVe);
            chiTiet.setGheNgoi(ghe);
            chiTiet.setLichChieu(lichChieu);
            chiTiet.setGiaTien(lichChieu.getGiaCoBan().multiply(ghe.getHeSoGia()));
            chiTiet.setHeSoGiaLucDat(ghe.getHeSoGia());
            chiTietDatGheRepository.save(chiTiet);
        }

        // 14. Lưu chi tiết sản phẩm
        for (ChiTietDatSanPham chiTiet : chiTietComboList) {
            chiTiet.setDatVe(savedDatVe);
            chiTietDatSanPhamRepository.save(chiTiet);
        }

        return savedDatVe;
    }

    /**
     * Lấy lịch sử đặt vé của user
     */
    public List<DatVe> getBookingHistory(Long userId) {
        return datVeRepository.findByNguoiDungId(userId);
    }

    /**
     * Lấy chi tiết một đơn đặt vé
     */
    public DatVe getBookingDetail(Long bookingId) {
        return datVeRepository.findById(bookingId).orElse(null);
    }

    /**
     * Hủy đơn đặt vé
     */
    @Transactional
    public void cancelBooking(Long bookingId, Long userId) {
        DatVe datVe = datVeRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Đơn đặt vé không tồn tại"));

        // Kiểm tra quyền (chỉ user chủ sở hữu mới được hủy)
        if (!datVe.getNguoiDung().getId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền hủy đơn này");
        }

        // Chỉ được hủy nếu chưa thanh toán
        if ("paid".equalsIgnoreCase(datVe.getTrangThaiThanhToan())) {
            throw new IllegalArgumentException("Không thể hủy đơn đã thanh toán");
        }

        datVe.setTrangThai("cancelled");
        datVeRepository.save(datVe);
    }

    /**
     * Generate mã đặt vé: "BK" + timestamp + random
     */
    private String generateBookingCode() {
        long timestamp = System.currentTimeMillis();
        int random = new Random().nextInt(1000);
        return String.format("BK%d%03d", timestamp % 1000000, random);
    }
}
